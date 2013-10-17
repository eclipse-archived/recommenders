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
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.ENCLOSING_METHOD_FIRST_DECLARATION;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.RECOMMENDERS_SCORE;
import static org.eclipse.recommenders.completion.rcp.processable.ProcessableCompletionProposalComputer.NULL_PROPOSAL;
import static org.eclipse.recommenders.completion.rcp.processable.Proposals.overlay;
import static org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.*;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OVR_STAR;
import static org.eclipse.recommenders.utils.Recommendations.*;

import java.util.Collections;
import java.util.HashSet;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.calls.NullCallModel;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

@SuppressWarnings({ "serial", "restriction" })
public class CallCompletionSessionProcessor extends SessionProcessor {

    private final Set<Class<?>> supportedCompletionRequests = new HashSet<Class<?>>() {
        {
            add(CompletionOnMemberAccess.class);
            add(CompletionOnMessageSend.class);
            add(CompletionOnQualifiedNameReference.class);
            add(CompletionOnSingleNameReference.class);
        }
    };
    private final ICallModelProvider modelProvider;
    private final IProjectCoordinateProvider pcProvider;

    private IRecommendersCompletionContext ctx;

    private UniqueTypeName name;
    private ICallModel model;

    private Iterable<Recommendation<IMethodName>> recommendations;

    private CallsRcpPreferences prefs;
    private ImageDescriptor overlay;

    @Inject
    public CallCompletionSessionProcessor(final IProjectCoordinateProvider pcProvider,
            final ICallModelProvider modelProvider, CallsRcpPreferences prefs, SharedImages images) {
        this.pcProvider = pcProvider;
        this.modelProvider = modelProvider;
        this.prefs = prefs;
        overlay = images.getDescriptor(OVR_STAR);
    }

    @Override
    public boolean startSession(final IRecommendersCompletionContext context) {
        ctx = context;
        recommendations = Lists.newLinkedList();
        try {
            if (!isCompletionRequestSupported() || //
                    !findReceiverTypeAndModel() || //
                    !findRecommendations()) {
                return false;
            }
            return true;
        } finally {
            releaseModel();
        }
    }

    private boolean findReceiverTypeAndModel() {
        IType receiverType = ctx.get(RECEIVER_TYPE2, null);
        if (receiverType == null) {
            return false;
        }
        name = pcProvider.toUniqueName(receiverType).orNull();
        if (name == null) {
            return false;
        }
        // TODO loop until we find a model. later
        model = modelProvider.acquireModel(name).or(NullCallModel.NULL_MODEL);
        return model != null;

    }

    private boolean isCompletionRequestSupported() {
        final ASTNode node = ctx.getCompletionNode().orNull();
        return node == null ? false : supportedCompletionRequests.contains(node.getClass());
    }

    private boolean findRecommendations() {
        // set override-context:
        IMethod overrides = ctx.get(ENCLOSING_METHOD_FIRST_DECLARATION, null);
        if (overrides != null) {
            IMethodName crOverrides = pcProvider.toName(overrides).or(
                    org.eclipse.recommenders.utils.Constants.UNKNOWN_METHOD);
            model.setObservedOverrideContext(crOverrides);
        }

        // set definition-type and defined-by
        model.setObservedDefinitionKind(ctx.get(RECEIVER_DEF_TYPE, null));
        model.setObservedDefiningMethod(ctx.get(RECEIVER_DEF_BY, null));
        // set calls:
        model.setObservedCalls(newHashSet(ctx.get(RECEIVER_CALLS, Collections.<IMethodName>emptyList())));

        // read
        recommendations = model.recommendCalls();
        // filter void methods if needed:
        if (ctx.getExpectedTypeSignature().isPresent()) {
            recommendations = Recommendations.filterVoid(recommendations);
        }
        recommendations = top(recommendations, prefs.maxNumberOfProposals, prefs.minProposalProbability / (double) 100);
        return !isEmpty(recommendations);
    }

    private void releaseModel() {
        if (model != null) {
            modelProvider.releaseModel(model);
        }
    }

    @Override
    public void process(final IProcessableProposal proposal) {
        if (isEmpty(recommendations)) {
            return;
        }

        final CompletionProposal coreProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (coreProposal.getKind()) {
        case CompletionProposal.METHOD_REF:
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.METHOD_NAME_REFERENCE:
            final ProposalMatcher matcher = new ProposalMatcher(coreProposal);
            for (final Recommendation<IMethodName> call : recommendations) {
                final IMethodName crMethod = call.getProposal();
                if (!matcher.match(crMethod)) {
                    continue;
                }
                int relevance = 0;
                if (prefs.changeProposalRelevance) {
                    relevance = 200 + asPercentage(call);
                    proposal.setTag(RECOMMENDERS_SCORE, asPercentage(call));
                }
                String label = "";
                if (prefs.decorateProposalText) {
                    label = asPercentage(call) + " %";
                }
                if (prefs.decorateProposalIcon) {
                    overlay(proposal, overlay);
                }
                ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
                mgr.addProcessor(new SimpleProposalProcessor(relevance, label));
                // we found the proposal we are looking for. So quit.
                break;
            }
        }
    }

    @VisibleForTesting
    public ICallModel getModel() {
        return model;
    }
}
