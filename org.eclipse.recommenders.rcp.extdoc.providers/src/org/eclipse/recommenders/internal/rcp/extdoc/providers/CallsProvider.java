/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.util.Sets;

@SuppressWarnings("restriction")
public final class CallsProvider extends AbstractProviderComposite {

    private final CallsModelStore modelStore;
    private final Provider<Set<IVariableUsageResolver>> usageResolversProvider;
    private final IntelligentCompletionContextResolver contextResolver;
    private final CallsServer server;

    private IIntelligentCompletionContext context;

    private Composite composite;
    private TextAndFeaturesLine line;
    private Composite calls;
    private StyledText line2;
    private Composite templates;

    @Inject
    public CallsProvider(final CallsModelStore modelStore,
            final Provider<Set<IVariableUsageResolver>> usageResolversProvider,
            final IntelligentCompletionContextResolver contextResolver, final CallsServer server) {
        this.modelStore = modelStore;
        this.usageResolversProvider = usageResolversProvider;
        this.contextResolver = contextResolver;
        this.server = server;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    protected boolean updateContent(final IJavaElementSelection selection) {
        if (selection.getInvocationContext() == null) {
            return false;
        }
        context = contextResolver.resolveContext(selection.getInvocationContext());
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof ILocalVariable) {
            createFakeContextForLocalVariableSelection(element);
        } else if (element instanceof IField) {
            createFakeContextForFieldSelection(element);
        } else if (element instanceof IType) {
            context = new DelegatingIntelligentCompletionContext(context) {
                @Override
                public Variable getVariable() {
                    return null;
                };
            };
        }

        //
        //

        if (context.getVariable() != null) {
            return displayProposalsForVariable(element, context.getVariable());
        } else if (element instanceof IType) {
            return displayProposalsForType((IType) element);
        } else if (element instanceof IMethod) {
            return displayProposalsForMethod((IMethod) element);
        } else if (element instanceof ILocalVariable || element instanceof SourceField) {
            try {
                return displayProposalsForUnresolvedVariable(element);
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    private void createFakeContextForFieldSelection(final IJavaElement element) {
        final IField f = (IField) element;
        final String name = f.getElementName();
        final IType declaringType = f.getDeclaringType();
        try {
            final String typeSignature = f.getTypeSignature();
            final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
            final IJavaProject javaProject = f.getJavaProject();
            final IType fieldType = javaProject.findType(resolvedTypeName);
            createFakeContext(name, fieldType, false);
        } catch (final JavaModelException e) {
            throwUnhandledException(e);
        }
    }

    private void createFakeContextForLocalVariableSelection(final IJavaElement element) {
        final ILocalVariable var = (ILocalVariable) element;
        final String name = element.getElementName();
        final IType declaringType = (IType) var.getAncestor(IJavaElement.TYPE);
        final String typeSignature = var.getTypeSignature();
        try {
            final IType variableType = resolveTypeSignature(var, declaringType, typeSignature);
            createFakeContext(name, variableType, false);
        } catch (final JavaModelException e) {
            throwUnhandledException(e);
        }
    }

    private IType resolveTypeSignature(final ILocalVariable var, final IType declaringType, final String typeSignature)
            throws JavaModelException {
        final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
        final IJavaProject javaProject = var.getJavaProject();
        final IType variableType = javaProject.findType(resolvedTypeName);
        return variableType;
    }

    private void createFakeContext(final String varName, final IType variableType, final boolean isArgument) {

        context = new DelegatingIntelligentCompletionContext(context) {

            @Override
            public Variable getVariable() {
                return Variable.create(varName, JavaElementResolver.INSTANCE.toRecType(variableType),
                        getEnclosingMethod());
            };
        };
    }

    private boolean displayProposalsForVariable(final IJavaElement element, final Variable variable) {
        System.err.println("displayProposalsForVariable: " + variable);
        if (modelStore.hasModel(variable.type)) {
            final IObjectMethodCallsNet model = getModel(variable.type, resolveCalledMethods());
            final boolean success = displayProposals(element, model.getRecommendedMethodCalls(0.01, 5));
            modelStore.releaseModel(model);
            return success;
        }
        return false;
    }

    private Set<IMethodName> resolveCalledMethods() {
        for (final IVariableUsageResolver resolver : usageResolversProvider.get()) {
            if (resolver.canResolve(context)) {
                final Set<IMethodName> receiverMethodInvocations = resolver.getReceiverMethodInvocations();
                return receiverMethodInvocations;
            }
        }
        return Sets.newHashSet();
    }

    private boolean displayProposalsForType(final IType type) {
        System.err.println("displayProposalsForType");
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecType(type);
        if (modelStore.hasModel(typeName)) {
            final IObjectMethodCallsNet model = getModel(typeName, new HashSet<IMethodName>());
            final boolean success = displayProposals(type, model.getRecommendedMethodCalls(0.01, 5));
            modelStore.releaseModel(model);
            return success;
        }
        return false;
    }

    private boolean displayProposalsForMethod(final IMethod method) {
        System.err.println("displayProposalsForMethod");

        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecMethod(method).getDeclaringType();
        if (modelStore.hasModel(typeName)) {
            final IObjectMethodCallsNet model = getModel(typeName, new HashSet<IMethodName>());
            final boolean success = displayProposals(method, model.getRecommendedMethodCalls(0.01, 5));
            modelStore.releaseModel(model);
            return success;
        } else {
            IMethod findOverriddenMethod;
            try {
                findOverriddenMethod = SuperTypeHierarchyCache.getMethodOverrideTester(method.getDeclaringType())
                        .findOverriddenMethod(method, true);
                if (findOverriddenMethod != null) {
                    return displayProposalsForMethod(findOverriddenMethod);
                }
            } catch (final JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean displayProposalsForUnresolvedVariable(final IJavaElement element) throws JavaModelException {
        System.err.println("displayProposalsForUnresolvedVariable");
        final ITypeName simpleTypeName = findVariableType(element);
        if (simpleTypeName != null) {
            for (final ITypeName typeName : modelStore.findTypesBySimpleName(simpleTypeName)) {
                final IObjectMethodCallsNet model = getModel(typeName, new HashSet<IMethodName>());
                final boolean success = displayProposals(element, model.getRecommendedMethodCalls(0.01, 5));
                if (success) {
                    displayTemplates(element, model);
                }
                modelStore.releaseModel(model);
                return success;
            }
        }
        return false;
    }

    private ITypeName findVariableType(final IJavaElement element) throws JavaModelException {
        final String signature;
        if (element instanceof SourceField) {
            signature = ((SourceField) element).getTypeSignature();
        } else {
            signature = ((ILocalVariable) element).getTypeSignature();
        }
        if (signature.length() < 4) {
            return null;
        }
        return VmTypeName.get(signature.substring(0, signature.length() - 1));
    }

    private IObjectMethodCallsNet getModel(final ITypeName typeName, final Set<IMethodName> invokedMethods) {
        final IObjectMethodCallsNet model = modelStore.acquireModel(typeName);
        model.clearEvidence();
        model.setMethodContext(context == null ? null : context.getEnclosingMethodsFirstDeclaration());
        System.err.println("invoked: " + invokedMethods);
        model.setObservedMethodCalls(typeName, invokedMethods);
        model.updateBeliefs();
        return model;
    }

    private boolean displayProposals(final IJavaElement element, final SortedSet<Tuple<IMethodName, Double>> proposals) {
        if (proposals.isEmpty()) {
            return false;
        }
        if (calls != null) {
            calls.dispose();
            line.dispose();
            if (templates != null) {
                templates.dispose();
                line2.dispose();
            }
        }

        final String text = "People who use " + element.getElementName() + " usually also call the following methods:";
        line = new TextAndFeaturesLine(composite, text, element, element.getElementName(), this, server,
                new TemplateEditDialog(getShell()));
        line.createStyleRange(15, element.getElementName().length(), SWT.NORMAL, false, true);

        calls = SwtFactory.createGridComposite(composite, 3, 12, 2, 12, 0);
        for (final Tuple<IMethodName, Double> proposal : proposals) {
            SwtFactory.createSquare(calls);
            final IMethodName method = proposal.getFirst();
            final String prefix = method.isInit() ? "new " : method.getDeclaringType().getClassName() + ".";
            SwtFactory.createLabel(calls, prefix + Names.vm2srcSimpleMethod(method), false, false, true);
            SwtFactory.createLabel(calls, Math.round(proposal.getSecond() * 100) + "%", false, true, false);
        }

        composite.layout(true);
        return true;
    }

    private void displayTemplates(final IJavaElement element, final IObjectMethodCallsNet model) {
        line2 = SwtFactory.createStyledText(composite, "Also the following patterns use to occur for this argument:");

        templates = SwtFactory.createGridComposite(composite, 1, 0, 12, 0, 0);
        for (final Tuple<String, Double> proposal : getTemplateProposals(model)) {
            model.setPattern(proposal.getFirst());
            model.updateBeliefs();
            final SortedSet<Tuple<IMethodName, Double>> methods = model.getRecommendedMethodCalls(0.01);
            if (methods.size() < 2) {
                continue;
            }

            final int probability = (int) Math.round(proposal.getSecond() * 100.0);
            final TextAndFeaturesLine text = new TextAndFeaturesLine(templates, proposal.getFirst() + " - "
                    + probability + "%", element, element.getElementName(), this, server, new TemplateEditDialog(
                    getShell()));
            text.createStyleRange(proposal.getFirst().length() + 3, String.valueOf(probability).length() + 1,
                    SWT.NORMAL, true, false);

            final Composite template = SwtFactory.createGridComposite(templates, 1, 12, 0, 12, 0);
            for (final Tuple<IMethodName, Double> method : methods) {
                SwtFactory.createLabel(template, Names.vm2srcSimpleMethod(method.getFirst()), false, false, true);
            }
        }

        composite.layout(true);
    }

    private Collection<Tuple<String, Double>> getTemplateProposals(final IObjectMethodCallsNet model) {
        model.clearEvidence();
        model.setMethodContext(null);
        model.negateConstructors();
        model.updateBeliefs();
        final Collection<Tuple<String, Double>> filtered = Collections2.filter(model.getPatternsWithProbability(),
                new Predicate<Tuple<String, Double>>() {
                    @Override
                    public boolean apply(final Tuple<String, Double> input) {
                        return input.getSecond() > 0.05;
                    }
                });
        return filtered;
    }
}
