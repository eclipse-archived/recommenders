/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.codecompletion.CompletionProposalDecorator;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("restriction")
public class CallsCompletionProposalComputer implements IJavaCompletionProposalComputer {
    private static final double MIN_PROBABILITY_THRESHOLD = 0.1d;
    private Set<Class<?>> supportedCompletionRequests;

    private final CallsModelStore modelsStore;

    private IIntelligentCompletionContext ctx;

    private Collection<CallsRecommendation> recommendations;

    private List<IJavaCompletionProposal> proposals;

    private final Provider<Set<IVariableUsageResolver>> usageResolversProvider;

    private Set<IMethodName> receiverMethodInvocations;

    private ITypeName receiverType;

    private IObjectMethodCallsNet model;

    private Variable receiver;

    private IMethodName firstMethodDeclaration;

    private final IntelligentCompletionContextResolver contextResolver;

    @Inject
    public CallsCompletionProposalComputer(final CallsModelStore modelsStore,
            final Provider<Set<IVariableUsageResolver>> usageResolversProvider,
            final IntelligentCompletionContextResolver contextResolver) {
        this.modelsStore = modelsStore;
        this.usageResolversProvider = usageResolversProvider;
        this.contextResolver = contextResolver;
        initializeSuportedCompletionRequests();
    }

    private void initializeSuportedCompletionRequests() {
        supportedCompletionRequests = Sets.newHashSet();
        supportedCompletionRequests.add(CompletionOnMemberAccess.class);
        supportedCompletionRequests.add(CompletionOnMessageSend.class);
        supportedCompletionRequests.add(CompletionOnQualifiedNameReference.class);
        supportedCompletionRequests.add(CompletionOnSingleNameReference.class);
    }

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        final JavaContentAssistInvocationContext jCtx = (JavaContentAssistInvocationContext) context;
        if (contextResolver.hasProjectRecommendersNature(jCtx)) {
            final IIntelligentCompletionContext iCtx = contextResolver.resolveContext(jCtx);
            return computeProposals(iCtx);
        } else {
            return Collections.emptyList();
        }
    }

    private List<IJavaCompletionProposal> computeProposals(final IIntelligentCompletionContext ctx) {
        this.ctx = ctx;
        if (!isCompletionRequestSupported()) {
            return emptyList();
        }
        if (!findReceiverInContext()) {
            return emptyList();
        }
        if (!findVariableUsageAndInvocationContext()) {
            return emptyList();
        }
        if (!findModel()) {
            return emptyList();
        }
        findRecommendations();
        findMatchingProposals();
        releaseModel();
        return this.proposals;
    }

    private boolean isCompletionRequestSupported() {
        final ASTNode node = ctx.getCompletionNode();
        return node == null ? false : supportedCompletionRequests.contains(node.getClass());
    }

    private boolean findReceiverInContext() {
        receiver = ctx.getVariable();
        return receiver != null;
    }

    private boolean findVariableUsageAndInvocationContext() {
        firstMethodDeclaration = ctx.getEnclosingMethodsFirstDeclaration();
        receiverMethodInvocations = receiver.getReceiverCalls();
        for (final IVariableUsageResolver resolver : usageResolversProvider.get()) {
            if (resolver.canResolve(ctx)) {

                // XXX this is a bit dirty: we need to handle this
                // appropriately... here ctx.getVariable() makes a hack for the
                // recevier type which noone can understand in 3 months.
                receiverType = receiver.isThis() ? receiver.type : ctx.getReceiverType();
                receiverMethodInvocations = resolver.getReceiverMethodInvocations();
                receiver = resolver.getResolvedVariable();
                return true;
            }
        }
        return true;
    }

    private boolean findModel() {
        if (modelsStore.hasModel(receiverType)) {
            model = modelsStore.acquireModel(receiverType);
        }
        return model != null;
    }

    private void findRecommendations() {
        recommendations = Lists.newLinkedList();
        model.clearEvidence();
        model.setMethodContext(firstMethodDeclaration);
        model.setObservedMethodCalls(receiverType, receiverMethodInvocations);
        if (receiver.fuzzyIsParameter() || receiver.fuzzyIsDefinedByMethodReturn()) {
            model.negateConstructors();
        }
        model.updateBeliefs();
        // update
        final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = model.getRecommendedMethodCalls(
                MIN_PROBABILITY_THRESHOLD, 5);
        for (final Tuple<IMethodName, Double> recommended : recommendedMethodCalls) {
            final IMethodName method = recommended.getFirst();
            final Double probability = recommended.getSecond();
            if (ctx.expectsReturnValue() && method.isVoid()) {
                continue;
            }
            final CallsRecommendation recommendation = CallsRecommendation.create(receiver, method, probability);
            recommendations.add(recommendation);
        }
    }

    private void findMatchingProposals() {
        this.proposals = Lists.newLinkedList();
        for (final CompletionProposal eclProposal : ctx.getJdtProposals()) {
            switch (eclProposal.getKind()) {
            case CompletionProposal.METHOD_REF:
            case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
            case CompletionProposal.METHOD_NAME_REFERENCE:
                createCallProposalIfRecommended(eclProposal);
            }
        }
    }

    private void createCallProposalIfRecommended(final CompletionProposal proposal) {
        final String signature = String.valueOf(proposal.getSignature()).replace('.', '/');
        final String name = String.valueOf(proposal.getName());
        final String propSignature = (name + signature).replaceAll("<\\.>", "");
        for (final CallsRecommendation call : recommendations) {
            final String recSignature = call.method.getSignature();
            if (recSignature.equals(propSignature)) {
                final IJavaCompletionProposal javaProposal = ctx.toJavaCompletionProposal(proposal);
                final CompletionProposalDecorator decoratedProposal = new CompletionProposalDecorator(javaProposal,
                        call);
                proposals.add(decoratedProposal);
            }
        }
    }

    private void releaseModel() {
        if (model != null) {
            modelsStore.releaseModel(model);
            model = null;
        }
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {
    }
}
