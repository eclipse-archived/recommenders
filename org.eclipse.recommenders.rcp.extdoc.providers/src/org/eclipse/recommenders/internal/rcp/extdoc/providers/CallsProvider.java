/**
 * Copyright (c) 2011 Stefan Henss, and others.
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

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

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
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite2;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
    private final CallsServer server;

    private Composite composite;
    private TextAndFeaturesLine line;
    private Composite calls;
    private StyledText line2;
    private Composite templates;
    private IIntelligentCompletionContext context;

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
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    protected void hookInitalize(final IJavaElementSelection selection) {
        this.context = contextResolver.resolveContext(selection.getInvocationContext());
    }

    @Override
    protected boolean updateMethodBlockSelection(final IJavaElementSelection selection, final ILocalVariable local) {
        setLocalVariableContext(local);
        return displayProposalsForVariable(local);
    }

    @Override
    protected boolean updateMethodBlockSelection(final IJavaElementSelection selection, final IField field) {
        setFieldVariableContext(field);
        return displayProposalsForVariable(field);
    }

    @Override
    protected boolean updateMethodBlockSelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext();
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field) {
        setFieldVariableContext(field);
        return displayProposalsForVariable(field);
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext();
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        setThisVariableContext();
        return displayProposalsForMethod(method);
    }

    private void setNullVariableContext() {
        context = new DelegatingIntelligentCompletionContext(context) {
            @Override
            public Variable getVariable() {
                return null;
            };
        };
    }

    private void setThisVariableContext() {
        context = new DelegatingIntelligentCompletionContext(context) {
            @Override
            public Variable getVariable() {
                return Variable.create("this", null, context.getEnclosingMethod());
            };
        };
    }

    private void setFieldVariableContext(final IField element) {
        final IField f = element;
        final String name = f.getElementName();
        final IType declaringType = f.getDeclaringType();
        try {
            final String typeSignature = f.getTypeSignature();
            final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
            final IJavaProject javaProject = f.getJavaProject();
            final IType fieldType = javaProject.findType(resolvedTypeName);
            setMockedContext(name, fieldType, false);
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

    private void setMockedContext(final String varName, final IType variableType, final boolean isArgument) {

        context = new DelegatingIntelligentCompletionContext(context) {

            @Override
            public Variable getVariable() {
                return Variable.create(varName, JavaElementResolver.INSTANCE.toRecType(variableType),
                        getEnclosingMethod());
            };
        };
    }

    private void setLocalVariableContext(final ILocalVariable var) {
        final String name = var.getElementName();
        final IType declaringType = (IType) var.getAncestor(IJavaElement.TYPE);
        final String typeSignature = var.getTypeSignature();
        try {
            final IType variableType = resolveTypeSignature(var, declaringType, typeSignature);
            setMockedContext(name, variableType, false);
        } catch (final JavaModelException e) {
            throwUnhandledException(e);
        }
    }

    private boolean displayProposalsForVariable(final IJavaElement element) {
        final Variable variable = context.getVariable();
        System.err.println("displayProposalsForVariable: " + variable);
        if (modelStore.hasModel(variable.type)) {
            Set<IMethodName> resolveCalledMethods = resolveCalledMethods();
            final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = computeRecommendations(variable.type,
                    resolveCalledMethods);
            final boolean success = displayProposals(element, recommendedMethodCalls);
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
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecType(type);
        if (modelStore.hasModel(typeName)) {
            final SortedSet<Tuple<IMethodName, Double>> calls = computeRecommendations(typeName,
                    new HashSet<IMethodName>());
            return displayProposals(type, calls);
        }
        return false;
    }

    private boolean displayProposalsForMethod(final IMethod method) {
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecMethod(method).getDeclaringType();
        if (modelStore.hasModel(typeName)) {
            final Set<IMethodName> resolveCalledMethods = resolveCalledMethods();
            final SortedSet<Tuple<IMethodName, Double>> calls = computeRecommendations(typeName, resolveCalledMethods);
            final boolean success = displayProposals(method, calls);
            return success;
        } else {
            IMethod findOverriddenMethod;
            try {
                final IType declaringType = method.getDeclaringType();
                final MethodOverrideTester tester = SuperTypeHierarchyCache.getMethodOverrideTester(declaringType);
                findOverriddenMethod = tester.findOverriddenMethod(method, true);
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

    private SortedSet<Tuple<IMethodName, Double>> computeRecommendations(final ITypeName typeName,
            final Set<IMethodName> invokedMethods) {
        final IObjectMethodCallsNet model = modelStore.acquireModel(typeName);
        model.clearEvidence();
        model.setMethodContext(context == null ? null : context.getEnclosingMethodsFirstDeclaration());
        System.err.println("invoked: " + invokedMethods);
        model.setObservedMethodCalls(typeName, invokedMethods);
        model.updateBeliefs();
        final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = model.getRecommendedMethodCalls(0.01, 5);
        modelStore.releaseModel(model);
        return recommendedMethodCalls;
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
}
