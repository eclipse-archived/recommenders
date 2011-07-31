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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ContextFactory;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.MockedIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractLocationSensitiveProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.CommunityFeatures;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.UIJob;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.util.Sets;

@SuppressWarnings("restriction")
public final class CallsProvider extends AbstractLocationSensitiveProviderComposite {

    private final CallsModelStore modelStore;
    private final Provider<Set<IVariableUsageResolver>> usageResolversProvider;
    private final GenericServer server;

    private Composite composite;
    private IIntelligentCompletionContext context;

    @Inject
    CallsProvider(final CallsModelStore modelStore, final Provider<Set<IVariableUsageResolver>> usageResolversProvider,
            final GenericServer server) {
        this.modelStore = modelStore;
        this.usageResolversProvider = usageResolversProvider;
        this.server = Preconditions.checkNotNull(server);
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    protected void hookInitalize(final IJavaElementSelection selection) {
        context = new MockedIntelligentCompletionContext(selection);
    }

    @Override
    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        context = ContextFactory.setNullVariableContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName());
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field) {
        context = ContextFactory.setFieldVariableContext(selection, field);
        if (context == null) {
            return false;
        }
        return displayProposalsForVariable(field, false, getProposalsFromSingleMethods(selection, field));
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        context = ContextFactory.setNullVariableContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName());
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        context = ContextFactory.setThisVariableContext(selection, method);
        return displayProposalsForMethod(method, true);
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName());
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName());
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local) {
        context = ContextFactory.setLocalVariableContext(selection, local);
        return context == null ? false : displayProposalsForVariable(local, true, null);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final ILocalVariable local) {
        context = ContextFactory.setLocalVariableContext(selection, local);
        return context == null ? false : displayProposalsForVariable(local, false, null);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IField field) {
        context = ContextFactory.setFieldVariableContext(selection, field);
        return context == null ? false : displayProposalsForVariable(field, false, null);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IMethod method) {
        context = ContextFactory.setNullVariableContext(selection);
        final Set<IMethodName> invokedMethods = ImmutableSet.of(ElementResolver.toRecMethod(method));
        final ITypeName receiverType = context.getReceiverType();
        return displayProposalsForType(
                receiverType == null ? method.getDeclaringType() : ElementResolver.toJdtType(receiverType),
                invokedMethods, method.getElementName());
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IType type) {
        context = ContextFactory.setNullVariableContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName());
    }

    private boolean displayProposalsForVariable(final IJavaElement element, final boolean negateConstructors,
            final SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> maxProbabilityFromMethods) {
        final Variable variable = context.getVariable();
        if (variable != null && modelStore.hasModel(variable.type)) {
            final Set<IMethodName> resolveCalledMethods = resolveCalledMethods();
            final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = computeRecommendations(variable.type,
                    resolveCalledMethods, negateConstructors);
            final IName name = element instanceof IField ? ElementResolver.toRecField((IField) element, variable.type)
                    : variable.getName();
            return displayProposals(element, element.getElementName(), name, false, recommendedMethodCalls,
                    resolveCalledMethods, maxProbabilityFromMethods);
        }
        return false;
    }

    private boolean displayProposalsForType(final IType type, final Set<IMethodName> invokedMethods,
            final String elementName) {
        final ITypeName typeName = ElementResolver.toRecType(type);
        if (modelStore.hasModel(typeName)) {
            final SortedSet<Tuple<IMethodName, Double>> calls = computeRecommendations(typeName, invokedMethods, false);
            return displayProposals(type, elementName, typeName, false, calls, new HashSet<IMethodName>(), null);
        }
        return false;
    }

    private boolean displayProposalsForMethod(final IMethod method, final boolean isMethodDeclaration) {
        final ITypeName type = getMethodsDeclaringType(method);
        if (type != null && modelStore.hasModel(type)) {
            final Set<IMethodName> calledMethods = resolveCalledMethods();
            final SortedSet<Tuple<IMethodName, Double>> calls = computeRecommendations(type, calledMethods, true);
            return displayProposals(method, method.getElementName(), ElementResolver.toRecMethod(method),
                    isMethodDeclaration, calls, calledMethods, null);
        } else {
            // TODO: first is not correct in all cases. this needs to be
            // fixed
            final IMethod first = JdtUtils.findFirstDeclaration(method);
            return first.equals(method) ? false : displayProposalsForMethod(first, isMethodDeclaration);
        }
    }

    private SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> getProposalsFromSingleMethods(
            final IJavaElementSelection selection, final IField field) {
        try {
            final Map<IMethodName, Tuple<IMethod, Double>> maxProbs = new HashMap<IMethodName, Tuple<IMethod, Double>>();
            final ITypeName fieldType = context.getVariable().type;
            if (!modelStore.hasModel(fieldType)) {
                return null;
            }
            for (final IMethod method : field.getDeclaringType().getMethods()) {
                context = ContextFactory.setLocalVariableContext(selection, field.getElementName(), fieldType,
                        ElementResolver.toRecMethod(method));
                for (final Tuple<IMethodName, Double> call : computeRecommendations(fieldType,
                        new HashSet<IMethodName>(), true)) {
                    if (!maxProbs.containsKey(call.getFirst())
                            || maxProbs.get(call.getFirst()).getSecond() < call.getSecond()) {
                        maxProbs.put(call.getFirst(), Tuple.create(method, call.getSecond()));
                    }
                }
            }
            final SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> sorted = new TreeSet<Tuple<IMethodName, Tuple<IMethodName, Double>>>(
                    new Comparator<Tuple<IMethodName, Tuple<IMethodName, Double>>>() {
                        @Override
                        public int compare(final Tuple<IMethodName, Tuple<IMethodName, Double>> arg0,
                                final Tuple<IMethodName, Tuple<IMethodName, Double>> arg1) {
                            return arg1.getSecond().getSecond().compareTo(arg0.getSecond().getSecond());
                        }
                    });
            for (final Entry<IMethodName, Tuple<IMethod, Double>> methodCall : maxProbs.entrySet()) {
                sorted.add(Tuple.create(methodCall.getKey(), Tuple.create(ElementResolver.toRecMethod(methodCall
                        .getValue().getFirst()), methodCall.getValue().getSecond())));
            }
            return sorted;
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private ITypeName getMethodsDeclaringType(final IMethod method) {
        try {
            final String superclassTypeSignature = method.getDeclaringType().getSuperclassTypeSignature();
            if (superclassTypeSignature == null) {
                return null;
            }
            final String superclassTypeName = JavaModelUtil.getResolvedTypeName(superclassTypeSignature,
                    method.getDeclaringType());
            final IType supertype = method.getJavaProject().findType(superclassTypeName);
            return ElementResolver.toRecType(supertype);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private Set<IMethodName> resolveCalledMethods() {
        for (final IVariableUsageResolver resolver : usageResolversProvider.get()) {
            if (resolver.canResolve(context)) {
                return resolver.getReceiverMethodInvocations();
            }
        }
        return Sets.newHashSet();
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

    private boolean displayProposals(final IJavaElement element, final String elementName, final IName elementId,
            final boolean isMethodDeclaration, final SortedSet<Tuple<IMethodName, Double>> proposals,
            final Set<IMethodName> calledMethods,
            final SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> maxProbabilitiesFromMethods) {
        if (proposals.isEmpty()) {
            return false;
        }

        final String action = isMethodDeclaration ? "declare" : "use";
        final String text = "People who " + action + " " + elementName + " usually also call the following methods"
                + (isMethodDeclaration ? " inside" : "") + ":";
        final String text2 = "When accessed from single methods, probabilites for this field's methods might be different:";
        final CommunityFeatures features = CommunityFeatures.create(elementId, null, this, server);

        new UIJob("Updating Calls Provider") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!composite.isDisposed()) {
                    disposeChildren(composite);
                    final TextAndFeaturesLine line = new TextAndFeaturesLine(composite, text, features);
                    line.createStyleRange(12 + action.length(), elementName.length(), SWT.NORMAL, false, true);
                    displayProposals(element, isMethodDeclaration, proposals, calledMethods);

                    if (maxProbabilitiesFromMethods != null) {
                        new TextAndFeaturesLine(composite, text2, features);
                        final Composite calls = SwtFactory.createGridComposite(composite, 4, 12, 2, 12, 0);
                        for (final Tuple<IMethodName, Tuple<IMethodName, Double>> proposal : maxProbabilitiesFromMethods) {
                            SwtFactory.createSquare(calls);
                            SwtFactory.createLabel(calls,
                                    formatMethodCall(element, proposal.getFirst(), isMethodDeclaration), false, true,
                                    SWT.COLOR_BLACK);
                            final int probability = (int) Math.round(proposal.getSecond().getSecond() * 100);
                            final String origin = Names.vm2srcSimpleMethod(proposal.getSecond().getFirst());
                            SwtFactory.createLabel(calls, probability + "%", false, false, SWT.COLOR_BLUE);
                            final StyledText styled = SwtFactory.createStyledText(calls, "in " + origin);
                            SwtFactory.createStyleRange(styled, 3, origin.length(), SWT.NORMAL, false, true);
                        }
                    }
                    composite.layout(true);
                    composite.getParent().getParent().layout(true);
                }
                return Status.OK_STATUS;
            }
        }.schedule();

        return true;
    }

    private void displayProposals(final IJavaElement element, final boolean isMethodDeclaration,
            final SortedSet<Tuple<IMethodName, Double>> proposals, final Set<IMethodName> calledMethods) {
        final Composite calls = SwtFactory.createGridComposite(composite, 3, 12, 2, 12, 0);
        for (final IMethodName method : calledMethods) {
            SwtFactory.createSquare(calls);
            SwtFactory.createLabel(calls, formatMethodCall(element, method, isMethodDeclaration), false, true,
                    SWT.COLOR_DARK_GRAY);
            SwtFactory.createLabel(calls, "(called)", false, false, SWT.COLOR_DARK_GRAY);
        }
        for (final Tuple<IMethodName, Double> proposal : proposals) {
            displayProposal(proposal, calls, element, isMethodDeclaration);
        }
    }

    private void displayProposal(final Tuple<IMethodName, Double> proposal, final Composite parent,
            final IJavaElement element, final boolean isMethodDeclaration) {
        SwtFactory.createSquare(parent);
        SwtFactory.createLabel(parent, formatMethodCall(element, proposal.getFirst(), isMethodDeclaration), false,
                true, SWT.COLOR_BLACK);
        SwtFactory.createLabel(parent, Math.round(proposal.getSecond() * 100) + "%", false, false, SWT.COLOR_BLUE);
    }

    private String formatMethodCall(final IJavaElement element, final IMethodName method,
            final boolean isMethodDeclaration) {
        final String prefix;
        final boolean isVariable = element instanceof ILocalVariable || element instanceof IField;
        if (isMethodDeclaration) {
            prefix = "this.";
        } else if (method.isInit()) {
            prefix = (isVariable ? element.getElementName() + " = " : "") + "new ";
        } else {
            prefix = (isVariable ? element.getElementName() : method.getDeclaringType().getClassName()) + ".";
        }
        return String.format("%s%s", prefix, Names.vm2srcSimpleMethod(method));
    }
}
