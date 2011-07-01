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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
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
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.MockedIntelligentCompletionContext;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.VariableResolver;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractLocationSensitiveProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.UIJob;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.util.Sets;

@SuppressWarnings("restriction")
public final class CallsProvider extends AbstractLocationSensitiveProviderComposite {

    private final CallsModelStore modelStore;
    private final Provider<Set<IVariableUsageResolver>> usageResolversProvider;
    private final JavaElementResolver elementResolver;
    private final CallsServer server;

    private Composite composite;
    private IIntelligentCompletionContext context;

    @Inject
    public CallsProvider(final CallsModelStore modelStore,
            final Provider<Set<IVariableUsageResolver>> usageResolversProvider,
            final JavaElementResolver elementResolver, final CallsServer server) {
        this.modelStore = modelStore;
        this.usageResolversProvider = usageResolversProvider;
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
        context = new MockedIntelligentCompletionContext(selection, elementResolver);
    }

    @Override
    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext(selection);
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final ILocalVariable local) {
        if (!setLocalVariableContext(selection, local)) {
            return false;
        }
        return displayProposalsForVariable(local, false);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IField field) {
        setFieldVariableContext(selection, field);
        return displayProposalsForVariable(field, false);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IMethod method) {
        try {
            if (method.isConstructor()) {
                return updateMethodBodySelection(selection, method.getDeclaringType());
            }
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
        return false;
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext(selection);
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field) {
        setFieldVariableContext(selection, field);
        if (!displayProposalsForVariable(field, false)) {
            return false;
        }
        displayProposalsForAllMethods(selection, field);
        return true;
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        setNullVariableContext(selection);
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        setThisVariableContext(selection, method);
        try {
            return displayProposalsForMethod(method);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        // TODO: this doesn't work yet because JDT fails to resolve the
        // enclosing method and throws an exception (for whatever reason)
        return displayProposalsForType(type);
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local) {
        if (!setLocalVariableContext(selection, local)) {
            return false;
        }
        return displayProposalsForVariable(local, true);
    }

    private void setNullVariableContext(final IJavaElementSelection selection) {
        context = new MockedIntelligentCompletionContext(selection, elementResolver) {
            @Override
            public Variable getVariable() {
                return null;
            };
        };
    }

    private void setThisVariableContext(final IJavaElementSelection selection, final IMethod enclosingMethod) {
        final IMethodName ctxEnclosingMethod = elementResolver.toRecMethod(enclosingMethod);
        final IMethodName ctxFirstDeclaration = elementResolver.toRecMethod(JdtUtils
                .findFirstDeclaration(enclosingMethod));

        context = new MockedIntelligentCompletionContext(selection, elementResolver) {
            @Override
            public IMethodName getEnclosingMethod() {
                return ctxEnclosingMethod;
            };

            @Override
            public IMethodName getEnclosingMethodsFirstDeclaration() {
                return ctxFirstDeclaration;
            };

            @Override
            public Variable getVariable() {
                return Variable.create("this", elementResolver.toRecType(enclosingMethod.getDeclaringType()),
                        elementResolver.toRecMethod(enclosingMethod));
            };
        };
    }

    private boolean setFieldVariableContext(final IJavaElementSelection selection, final IField element) {
        final IField field = element;
        final String name = field.getElementName();
        final IType declaringType = field.getDeclaringType();
        try {
            final String typeSignature = field.getTypeSignature();
            final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(typeSignature, declaringType);
            final IJavaProject javaProject = field.getJavaProject();
            final IType fieldType = javaProject.findType(resolvedTypeName);
            return setMockedContext(selection, name, fieldType, false);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean setMockedContext(final IJavaElementSelection selection, final String varName,
            final IType variableType, final boolean isArgument) {
        if (variableType == null) {
            return false;
        }
        context = new MockedIntelligentCompletionContext(selection, elementResolver) {
            @Override
            public Variable getVariable() {
                return Variable.create(varName, elementResolver.toRecType(variableType), getEnclosingMethod());
            };
        };
        return true;
    }

    private boolean setLocalVariableContext(final IJavaElementSelection selection, final ILocalVariable var) {
        final String name = var.getElementName();
        final IType variableType = VariableResolver.resolveTypeSignature(var);
        return setMockedContext(selection, name, variableType, false);
    }

    private Set<IMethodName> resolveCalledMethods() {
        for (final IVariableUsageResolver resolver : usageResolversProvider.get()) {
            if (resolver.canResolve(context)) {
                return resolver.getReceiverMethodInvocations();
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
            return displayProposals(element, recommendedMethodCalls, resolveCalledMethods);
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

    private boolean displayProposalsForMethod(final IMethod method) throws JavaModelException {
        final String superclassTypeSignature = method.getDeclaringType().getSuperclassTypeSignature();
        if (superclassTypeSignature == null) {
            return false;
        }
        final String superclassTypeName = JavaModelUtil.getResolvedTypeName(superclassTypeSignature,
                method.getDeclaringType());
        final IType supertype = method.getJavaProject().findType(superclassTypeName);
        final ITypeName type = JavaElementResolver.INSTANCE.toRecType(supertype);
        if (type != null && modelStore.hasModel(type)) {
            final Set<IMethodName> calledMethods = resolveCalledMethods();
            final SortedSet<Tuple<IMethodName, Double>> calls = computeRecommendations(type, calledMethods, true);
            return displayProposals(method, calls, calledMethods);
        } else {
            // TODO: first is not correct in all cases. this needs to be fixed
            final IMethod first = JdtUtils.findFirstDeclaration(method);
            return first.equals(method) ? false : displayProposalsForMethod(first);
        }
    }

    private void displayProposalsForAllMethods(final IJavaElementSelection selection, final IField field) {
        try {
            final ITypeName fieldType = context.getVariable().type;
            for (final IMethod method : field.getDeclaringType().getMethods()) {
                if (false) {
                    context = new MockedIntelligentCompletionContext(selection, elementResolver) {
                        @Override
                        public Variable getVariable() {
                            return Variable.create(field.getElementName(), fieldType,
                                    elementResolver.toRecMethod(method));
                        };
                    };
                    displayProposalsForMethod(method);
                }
            }
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
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

        final String text = "People who use " + element.getElementName() + " usually also call the following methods:";
        final CallsProvider provider = this;
        new UIJob("Updating Calls Provider") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                disposeChildren(composite);
                final TextAndFeaturesLine line = new TextAndFeaturesLine(composite, text, element,
                        element.getElementName(), provider, server, new TemplateEditDialog(getShell()));
                line.createStyleRange(15, element.getElementName().length(), SWT.NORMAL, false, true);
                displayProposals(proposals, calledMethods);
                composite.layout(true);
                return Status.OK_STATUS;
            }
        }.schedule();

        return true;
    }

    private void displayProposals(final SortedSet<Tuple<IMethodName, Double>> proposals,
            final Set<IMethodName> calledMethods) {
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
    }
}
