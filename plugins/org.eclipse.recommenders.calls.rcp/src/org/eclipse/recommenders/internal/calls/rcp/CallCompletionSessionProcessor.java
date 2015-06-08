/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.max;
import static java.math.RoundingMode.HALF_EVEN;
import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.ENCLOSING_METHOD_FIRST_DECLARATION;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.RECOMMENDERS_SCORE;
import static org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.*;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OVR_STAR;
import static org.eclipse.recommenders.utils.Constants.REASON_NOT_IN_CACHE;
import static org.eclipse.recommenders.utils.Recommendations.top;
import static org.eclipse.recommenders.utils.Result.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.completion.rcp.IProposalNameProvider;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.OverlayImageProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.internal.calls.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.models.rcp.PrefetchModelArchiveJob;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;

@SuppressWarnings({ "restriction" })
public class CallCompletionSessionProcessor extends SessionProcessor {

    private static final CompletionProposal NULL_PROPOSAL = new CompletionProposal();

    private final ImmutableSet<Class<? extends ASTNode>> supportedCompletionRequests = ImmutableSet
            .<Class<? extends ASTNode>>of(CompletionOnMemberAccess.class, CompletionOnMessageSend.class,
                    CompletionOnQualifiedNameReference.class, CompletionOnSingleNameReference.class);

    private final IProjectCoordinateProvider pcProvider;
    private final ICallModelProvider modelProvider;
    private final IProposalNameProvider methodNameProvider;
    private final CallsRcpPreferences prefs;
    private final OverlayImageProposalProcessor overlayProcessor;

    private IRecommendersCompletionContext ctx;
    private Iterable<Recommendation<IMethodName>> recommendations;
    private ICallModel model;

    private Set<IMethodName> observedCalls;

    private Map<Recommendation<IMethodName>, Integer> recommendationsIndex;


    @Inject
    public CallCompletionSessionProcessor(IProjectCoordinateProvider pcProvider, ICallModelProvider modelProvider,
            IProposalNameProvider methodNameProvider, CallsRcpPreferences prefs, SharedImages images) {
        this.pcProvider = pcProvider;
        this.modelProvider = modelProvider;
        this.methodNameProvider = methodNameProvider;
        this.prefs = prefs;
        this.overlayProcessor = new OverlayImageProposalProcessor(images.getDescriptor(OVR_STAR), IDecoration.TOP_LEFT);
    }

    @Override
    public boolean startSession(final IRecommendersCompletionContext context) {
        ctx = context;

        recommendations = Lists.newLinkedList();

        try {
            return isCompletionRequestSupported() && findReceiverTypeAndModel() && findRecommendations();
        } finally {
            releaseModel();
        }
    }

    private boolean isCompletionRequestSupported() {
        final ASTNode node = ctx.getCompletionNode().orNull();
        if (node == null) {
            return false;
        } else {
            for (Class<? extends ASTNode> supportedCompletionRequest : supportedCompletionRequests) {
                if (supportedCompletionRequest.isInstance(node)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean findReceiverTypeAndModel() {
        final IType receiverType = ctx.get(RECEIVER_TYPE2, null);
        if (receiverType == null) {
            return false;
        }
        Result<UniqueTypeName> res = pcProvider.tryToUniqueName(receiverType);
        switch (res.getReason()) {
        case OK:
            model = modelProvider.acquireModel(res.get()).orNull();
            return model != null;
        case REASON_NOT_IN_CACHE:
            new PrefetchModelArchiveJob<ICallModel>(receiverType, pcProvider, modelProvider).schedule(200);
        case ABSENT:
        default:
            return false;
        }
    }

    private boolean findRecommendations() {
        // set override-context:
        IMethod overrides = ctx.get(ENCLOSING_METHOD_FIRST_DECLARATION, null);
        if (overrides != null) {
            IMethodName crOverrides = pcProvider.toName(overrides)
                    .or(org.eclipse.recommenders.utils.Constants.UNKNOWN_METHOD);
            model.setObservedOverrideContext(crOverrides);
        }

        // set definition-type and defined-by
        model.setObservedDefinitionKind(ctx.get(RECEIVER_DEF_TYPE, null));
        model.setObservedDefiningMethod(ctx.get(RECEIVER_DEF_BY, null));
        observedCalls = newHashSet(ctx.get(RECEIVER_CALLS, Collections.<IMethodName>emptyList()));
        model.setObservedCalls(observedCalls);

        // read
        recommendations = model.recommendCalls();
        // filter void methods if needed:
        if (ctx.getExpectedTypeSignature().isPresent()) {
            recommendations = Recommendations.filterVoid(recommendations);
        }

        recommendations = top(recommendations, prefs.maxNumberOfProposals,
                max(prefs.minProposalPercentage, 0.01) / 100);
        calculateProposalRelevanceBoostMap();

        return !isEmpty(recommendations) || !observedCalls.isEmpty();
    }

    private void calculateProposalRelevanceBoostMap() {
        recommendationsIndex = Maps.newHashMap();
        for (Recommendation<IMethodName> r : recommendations) {
            double rel = r.getRelevance() * 100;
            int score = 0;
            if (rel < 1d) {
                int promille = DoubleMath.roundToInt(rel * 100, HALF_EVEN);
                score = promille;
            } else {
                score = 100 + DoubleMath.roundToInt(rel, HALF_EVEN);

            }
            recommendationsIndex.put(r, score);
        }
    }

    private void releaseModel() {
        if (model != null) {
            modelProvider.releaseModel(model);
        }
    }

    @Override
    public void process(final IProcessableProposal proposal) {
        final CompletionProposal coreProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (coreProposal.getKind()) {
        case CompletionProposal.METHOD_REF:
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.METHOD_NAME_REFERENCE:
            IMethodName proposedMethod = methodNameProvider.toMethodName(coreProposal).orNull();
            if (proposedMethod == null) {
                return;
            }

            final ProposalMatcher matcher = new ProposalMatcher(proposedMethod);
            if (prefs.highlightUsedProposals && handleAlreadyUsedProposal(proposal, matcher)) {
                return;
            }
            handleRecommendation(proposal, matcher);
        }
    }

    private boolean handleAlreadyUsedProposal(IProcessableProposal proposal, ProposalMatcher matcher) {
        for (IMethodName observed : observedCalls) {
            if (matcher.match(observed)) {
                final int boost = prefs.changeProposalRelevance ? 1 : 0;
                final String label = prefs.decorateProposalText ? Messages.PROPOSAL_LABEL_USED : ""; //$NON-NLS-1$
                ProposalProcessorManager manager = proposal.getProposalProcessorManager();
                manager.addProcessor(new SimpleProposalProcessor(boost, label));

                if (prefs.decorateProposalIcon) {
                    manager.addProcessor(overlayProcessor);
                }
                return true;
            }
        }
        return false;
    }

    private void handleRecommendation(IProcessableProposal proposal, ProposalMatcher matcher) {
        for (final Recommendation<IMethodName> call : recommendations) {
            final IMethodName crMethod = call.getProposal();
            if (!matcher.match(crMethod)) {
                continue;
            }

            Integer score = recommendationsIndex.get(call);
            final int boost = prefs.changeProposalRelevance ? 200 + score : 0;
            if (boost > 0) {
                proposal.setTag(RECOMMENDERS_SCORE, score);
            }

            String label = ""; //$NON-NLS-1$
            if (prefs.decorateProposalText) {
                double relevance = call.getRelevance();
                String format = relevance < 0.01d ? Messages.PROPOSAL_LABEL_PROMILLE
                        : Messages.PROPOSAL_LABEL_PERCENTAGE;
                label = format(format, relevance);
            }

            ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
            mgr.addProcessor(new SimpleProposalProcessor(boost, label));

            if (prefs.decorateProposalIcon) {
                mgr.addProcessor(overlayProcessor);
            }

            // we found the proposal we are looking for. So quit.
            return;
        }
    }

    @VisibleForTesting
    public ICallModel getModel() {
        return model;
    }
}
