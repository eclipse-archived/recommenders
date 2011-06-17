/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Marcel Bruch - refined implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

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
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.VariableResolver;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite2;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.util.Sets;

@SuppressWarnings("restriction")
public final class CallsProvider extends AbstractProviderComposite2 {

    private final CallsModelStore modelStore;
    private final Provider<Set<IVariableUsageResolver>> usageResolversProvider;
    private final IntelligentCompletionContextResolver contextResolver;
    private final JavaElementResolver elementResolver;
    private final CallsServer server;

    private Composite composite;
    private IIntelligentCompletionContext context;

    @Inject
    public CallsProvider(final CallsModelStore modelStore,
            final Provider<Set<IVariableUsageResolver>> usageResolversProvider,
            final IntelligentCompletionContextResolver contextResolver, final JavaElementResolver elementResolver,
            final CallsServer server) {
        this.modelStore = modelStore;
        this.usageResolversProvider = usageResolversProvider;
        this.contextResolver = contextResolver;
        this.elementResolver = elementResolver;
        this.server = server;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    protected void hookInitalize(final IJavaElementSelection selection) {
        context = contextResolver.resolveContext(selection.getInvocationContext());
    }

    @Override
    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext();
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final ILocalVariable local) {
        if (!setLocalVariableContext(local)) {
            return false;
        }
        return displayProposalsForVariable(local, false);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IField field) {
        setFieldVariableContext(field);
        return displayProposalsForVariable(field, false);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext();
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field) {
        setFieldVariableContext(field);
        return displayProposalsForVariable(field, false);
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext();
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        setThisVariableContext(method);
        return displayProposalsForMethod(method);
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        // TODO: this doesn't work yet because JDT fails to resolve the
        // enclosing method and throws an exception (for whatever reason)
        return false;
        // return displayProposalsForType(type);
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local) {
        if (!setLocalVariableContext(local)) {
            return false;
        }
        return displayProposalsForVariable(local, true);
    }

    private void setNullVariableContext() {
        context = new DelegatingIntelligentCompletionContext(context) {
            @Override
            public Variable getVariable() {
                return null;
            };
        };
    }

    private void setThisVariableContext(final IMethod enclosingMethod) {
        context = new DelegatingIntelligentCompletionContext(context) {
            @Override
            public Variable getVariable() {
                return Variable.create("this", null, elementResolver.toRecMethod(enclosingMethod));
            };
        };
    }

    private boolean setFieldVariableContext(final IField element) {
        final IField f = element;
        final String name = f.getElementName();
        final IType declaringType = f.getDeclaringType();
        try {
            final String typeSignature = f.getTypeSignature();
            final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
            final IJavaProject javaProject = f.getJavaProject();
            final IType fieldType = javaProject.findType(resolvedTypeName);
            return setMockedContext(name, fieldType, false);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean setMockedContext(final String varName, final IType variableType, final boolean isArgument) {
        if (variableType == null) {
            return false;
        }
        context = new DelegatingIntelligentCompletionContext(context) {
            @Override
            public Variable getVariable() {
                return Variable.create(varName, elementResolver.toRecType(variableType), getEnclosingMethod());
            };
        };
        return true;
    }

    private boolean setLocalVariableContext(final ILocalVariable var) {
        final String name = var.getElementName();
        final IType variableType = VariableResolver.resolveTypeSignature(var);
        return setMockedContext(name, variableType, false);
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

    private boolean displayProposalsForVariable(final IJavaElement element, final boolean negateConstructors) {
        final Variable variable = context.getVariable();
        if (variable != null && modelStore.hasModel(variable.type)) {
            final Set<IMethodName> resolveCalledMethods = resolveCalledMethods();
            final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = computeRecommendations(variable.type,
                    resolveCalledMethods, negateConstructors);
            final boolean success = displayProposals(element, recommendedMethodCalls, resolveCalledMethods);
            return success;
        }
        return false;
    }

    private boolean displayProposalsForType(final IType type) {
        final ITypeName typeName = elementResolver.toRecType(type);
        if (modelStore.hasModel(typeName)) {
            final SortedSet<Tuple<IMethodName, Double>> calls = computeRecommendations(typeName,
                    new HashSet<IMethodName>(), false);
            return displayProposals(type, calls, new HashSet<IMethodName>());
        }
        return false;
    }

    private boolean displayProposalsForMethod(final IMethod method) {
        final IMethodName methodName = elementResolver.toRecMethod(method);
        if (methodName == null) {
            return false;
        } else if (modelStore.hasModel(methodName.getDeclaringType())) {
            final Set<IMethodName> resolveCalledMethods = resolveCalledMethods();
            final SortedSet<Tuple<IMethodName, Double>> calls = computeRecommendations(methodName.getDeclaringType(),
                    resolveCalledMethods, false);
            return displayProposals(method, calls, resolveCalledMethods);
        } else {
            try {
                final IType declaringType = method.getDeclaringType();
                final MethodOverrideTester tester = SuperTypeHierarchyCache.getMethodOverrideTester(declaringType);
                final IMethod findOverriddenMethod = tester.findOverriddenMethod(method, true);
                if (findOverriddenMethod != null) {
                    return displayProposalsForMethod(findOverriddenMethod);
                }
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    private SortedSet<Tuple<IMethodName, Double>> computeRecommendations(final ITypeName typeName,
            final Set<IMethodName> invokedMethods, final boolean negateConstructors) {
        final IObjectMethodCallsNet model = modelStore.acquireModel(typeName);
        model.clearEvidence();
        model.setMethodContext(context == null ? null : context.getEnclosingMethodsFirstDeclaration());
        model.setObservedMethodCalls(typeName, invokedMethods);
        if (negateConstructors) {
            model.negateConstructors();
        }
        model.updateBeliefs();
        final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = model.getRecommendedMethodCalls(0.01, 5);
        modelStore.releaseModel(model);
        return recommendedMethodCalls;
    }

    private boolean displayProposals(final IJavaElement element, final SortedSet<Tuple<IMethodName, Double>> proposals,
            final Set<IMethodName> calledMethods) {
        if (proposals.isEmpty()) {
            return false;
        }
        disposeChildren(composite);

        final String text = "People who use " + element.getElementName() + " usually also call the following methods:";
        final TextAndFeaturesLine line = new TextAndFeaturesLine(composite, text, element, element.getElementName(),
                this, server, new TemplateEditDialog(getShell()));
        line.createStyleRange(15, element.getElementName().length(), SWT.NORMAL, false, true);

        final Composite calls = SwtFactory.createGridComposite(composite, 3, 12, 2, 12, 0);
        for (final IMethodName method : calledMethods) {
            SwtFactory.createSquare(calls);
            final String prefix = method.isInit() ? "new " : method.getDeclaringType().getClassName() + ".";
            SwtFactory.createLabel(calls, prefix + Names.vm2srcSimpleMethod(method), false, true, SWT.COLOR_DARK_GRAY);
            SwtFactory.createLabel(calls, "(called)", false, false, SWT.COLOR_DARK_GRAY);
        }
        for (final Tuple<IMethodName, Double> proposal : proposals) {
            SwtFactory.createSquare(calls);
            final IMethodName method = proposal.getFirst();
            final String prefix = method.isInit() ? "new " : method.getDeclaringType().getClassName() + ".";
            SwtFactory.createLabel(calls, prefix + Names.vm2srcSimpleMethod(method), false, true, SWT.COLOR_BLACK);
            SwtFactory.createLabel(calls, Math.round(proposal.getSecond() * 100) + "%", false, false, SWT.COLOR_BLUE);
        }

        composite.layout(true);
        return true;
    }
}
