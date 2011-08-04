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
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IProjectModelFacade;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectServices;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.CallsAdapter;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ContextFactory;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.MockedIntelligentCompletionContext;
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

public final class CallsProvider extends AbstractLocationSensitiveProviderComposite {

    private final GenericServer server;
    private final CallsAdapter adapter;
    private Composite composite;
    private final ProjectServices projectServices;

    @Inject
    CallsProvider(final ProjectServices projectServices,
            final Provider<Set<IVariableUsageResolver>> usageResolversProvider, final GenericServer server) {
        this.projectServices = projectServices;
        this.server = Preconditions.checkNotNull(server);
        adapter = new CallsAdapter(projectServices, usageResolversProvider);
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        final MockedIntelligentCompletionContext context = ContextFactory.createNullVariableContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName(), context);
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field) {
        final MockedIntelligentCompletionContext context = ContextFactory.createFieldVariableContext(selection, field);
        if (context == null) {
            return false;
        }
        return displayProposalsForVariable(field, false,
                adapter.getProposalsFromSingleMethods(selection, field, context), context);
    }

    @Override
    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        final MockedIntelligentCompletionContext context = ContextFactory.createNullVariableContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName(), context);
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        final MockedIntelligentCompletionContext context = ContextFactory.createThisVariableContext(selection, method);
        return displayProposalsForMethod(method, true, context);
    }

    @Override
    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        final MockedIntelligentCompletionContext context = new MockedIntelligentCompletionContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName(), context);
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        final MockedIntelligentCompletionContext context = new MockedIntelligentCompletionContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName(), context);
    }

    @Override
    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local) {
        final MockedIntelligentCompletionContext context = ContextFactory.createLocalVariableContext(selection, local);
        return context == null ? false : displayProposalsForVariable(local, true, null, context);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final ILocalVariable local) {
        final MockedIntelligentCompletionContext context = ContextFactory.createLocalVariableContext(selection, local);
        return context == null ? false : displayProposalsForVariable(local, false, null, context);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IField field) {
        final MockedIntelligentCompletionContext context = ContextFactory.createFieldVariableContext(selection, field);
        return context == null ? false : displayProposalsForVariable(field, false, null, context);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IMethod method) {
        final MockedIntelligentCompletionContext context = ContextFactory.createNullVariableContext(selection);
        final Set<IMethodName> invokedMethods = ImmutableSet.of(ElementResolver.toRecMethod(method));
        final ITypeName receiverType = context.getReceiverType();
        return displayProposalsForType(
                receiverType == null ? method.getDeclaringType() : ElementResolver.toJdtType(receiverType),
                invokedMethods, method.getElementName(), context);
    }

    @Override
    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IType type) {
        final MockedIntelligentCompletionContext context = ContextFactory.createNullVariableContext(selection);
        return displayProposalsForType(type, new HashSet<IMethodName>(), type.getElementName(), context);
    }

    private boolean displayProposalsForVariable(final IJavaElement element, final boolean negateConstructors,
            final SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> maxProbabilityFromMethods,
            final MockedIntelligentCompletionContext context) {
        final Variable variable = context.getVariable();
        final IProjectModelFacade facade = adapter.getModelFacade(element);
        if (variable != null && facade.hasModel(variable.type)) {
            final Set<IMethodName> resolveCalledMethods = adapter.resolveCalledMethods(context);
            final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = adapter.computeRecommendations(
                    variable.type, resolveCalledMethods, negateConstructors, context, facade);
            final IName name = element instanceof IField ? ElementResolver.toRecField((IField) element, variable.type)
                    : variable.getName();
            return displayProposals(element, element.getElementName(), name, false, recommendedMethodCalls,
                    resolveCalledMethods, maxProbabilityFromMethods);
        }
        return false;
    }

    private boolean displayProposalsForType(final IType type, final Set<IMethodName> invokedMethods,
            final String elementName, final MockedIntelligentCompletionContext context) {
        final ITypeName typeName = ElementResolver.toRecType(type);
        final IProjectModelFacade facade = adapter.getModelFacade(type);
        if (facade.hasModel(typeName)) {
            final SortedSet<Tuple<IMethodName, Double>> calls = adapter.computeRecommendations(typeName,
                    invokedMethods, false, context, facade);
            return displayProposals(type, elementName, typeName, false, calls, new HashSet<IMethodName>(), null);
        }
        return false;
    }

    private boolean displayProposalsForMethod(final IMethod method, final boolean isMethodDeclaration,
            final MockedIntelligentCompletionContext context) {
        final ITypeName type = adapter.getMethodsDeclaringType(method, context);
        final IProjectModelFacade facade = adapter.getModelFacade(method);
        if (type != null && facade.hasModel(type)) {
            final Set<IMethodName> calledMethods = adapter.resolveCalledMethods(context);
            final SortedSet<Tuple<IMethodName, Double>> calls = adapter.computeRecommendations(type, calledMethods,
                    true, context, facade);
            return displayProposals(method, method.getElementName(), ElementResolver.toRecMethod(method),
                    isMethodDeclaration, calls, calledMethods, null);
        } else {
            // TODO: first is not correct in all cases. this needs to be
            // fixed
            final IMethod first = JdtUtils.findFirstDeclaration(method);
            return first.equals(method) ? false : displayProposalsForMethod(first, isMethodDeclaration, context);
        }
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
                    composite.getParent().getParent().getParent().layout(true);
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
