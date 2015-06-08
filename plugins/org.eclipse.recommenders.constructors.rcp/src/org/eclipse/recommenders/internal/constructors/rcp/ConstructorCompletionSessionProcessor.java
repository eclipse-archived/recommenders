/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.internal.constructors.rcp;

import static java.math.RoundingMode.HALF_EVEN;
import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.RECOMMENDERS_SCORE;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OVR_STAR;
import static org.eclipse.recommenders.utils.Constants.REASON_NOT_IN_CACHE;
import static org.eclipse.recommenders.utils.Result.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedTypeReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.recommenders.completion.rcp.IProposalNameProvider;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.OverlayImageProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.constructors.ConstructorModel;
import org.eclipse.recommenders.constructors.IConstructorModelProvider;
import org.eclipse.recommenders.internal.constructors.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.models.rcp.PrefetchModelArchiveJob;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;

public class ConstructorCompletionSessionProcessor extends SessionProcessor {

    private final ImmutableSet<Class<? extends ASTNode>> supportedCompletionRequests = ImmutableSet
            .<Class<? extends ASTNode>>of(CompletionOnSingleTypeReference.class,
                    CompletionOnQualifiedTypeReference.class);

    private final IProjectCoordinateProvider pcProvider;
    private final IConstructorModelProvider modelProvider;
    private final IProposalNameProvider methodNameProvider;
    private final ConstructorsRcpPreferences prefs;
    private final OverlayImageProposalProcessor overlayProcessor;

    private Map<CompletionProposal, Double> recommationationsMap;

    @Inject
    public ConstructorCompletionSessionProcessor(IProjectCoordinateProvider pcProvider,
            IConstructorModelProvider modelProvider, IProposalNameProvider methodNameProvider,
            ConstructorsRcpPreferences prefs, SharedImages images) {
        this.pcProvider = requireNonNull(pcProvider);
        this.modelProvider = requireNonNull(modelProvider);
        this.methodNameProvider = requireNonNull(methodNameProvider);
        this.prefs = requireNonNull(prefs);
        this.overlayProcessor = new OverlayImageProposalProcessor(images.getDescriptor(OVR_STAR), IDecoration.TOP_LEFT);
    }

    @Override
    public boolean startSession(final IRecommendersCompletionContext context) {
        if (!isCompletionRequestSupported(context)) {
            return false;
        }

        IType expectedType = context.getExpectedType().orNull();
        if (expectedType == null) {
            return false;
        }

        final ConstructorModel model;
        Result<UniqueTypeName> res = pcProvider.tryToUniqueName(expectedType);
        switch (res.getReason()) {
        case OK:
            model = modelProvider.acquireModel(res.get()).orNull();
            break;
        case REASON_NOT_IN_CACHE:
            new PrefetchModelArchiveJob<ConstructorModel>(expectedType, pcProvider, modelProvider).schedule(200);
            // fall-through
        case ABSENT:
        default:
            return false;
        }

        try {
            if (model == null) {
                return false;
            }

            Map<IJavaCompletionProposal, CompletionProposal> proposals = context.getProposals();
            final Map<CompletionProposal, IMethodName> foundConstructors = Maps.newHashMap();
            int runningTotal = 0;
            for (Entry<IJavaCompletionProposal, CompletionProposal> entry : proposals.entrySet()) {
                CompletionProposal coreProposal = entry.getValue();
                if (coreProposal.getKind() != CompletionProposal.CONSTRUCTOR_INVOCATION) {
                    continue;
                }
                IMethodName methodName = methodNameProvider.toMethodName(coreProposal).orNull();
                if (methodName == null) {
                    continue;
                }
                if (!methodName.isInit()) {
                    continue;
                }
                int constructorCallCount = model.getConstructorCallCount(methodName);
                if (constructorCallCount == 0) {
                    continue;
                }
                foundConstructors.put(coreProposal, methodName);
                runningTotal += constructorCallCount;
            }
            final int foundConstructorsTotal = runningTotal;

            if (foundConstructorsTotal == 0) {
                return false;
            }

            Iterable<Recommendation<CompletionProposal>> recommendations = Iterables.transform(
                    foundConstructors.entrySet(),
                    new Function<Entry<CompletionProposal, IMethodName>, Recommendation<CompletionProposal>>() {

                        @Override
                        public Recommendation<CompletionProposal> apply(Entry<CompletionProposal, IMethodName> entry) {
                            IMethodName methodName = entry.getValue();
                            double relevance = model.getConstructorCallCount(methodName)
                                    / (double) foundConstructorsTotal;
                            return Recommendation.newRecommendation(entry.getKey(), relevance);
                        }
                    });

            List<Recommendation<CompletionProposal>> topRecommendations = Recommendations.top(recommendations,
                    prefs.maxNumberOfProposals, prefs.minProposalPercentage / 100.0);
            if (topRecommendations.isEmpty()) {
                return false;
            }

            recommationationsMap = Recommendations.asMap(topRecommendations);

            return true;
        } finally {
            modelProvider.releaseModel(model);
        }
    }

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        CompletionProposal coreProposal = proposal.getCoreProposal().orNull();
        if (coreProposal == null) {
            return;
        }

        Double relevance = recommationationsMap.get(coreProposal);
        if (relevance == null) {
            return;
        }

        final int score;
        if (relevance < 0.01) {
            score = DoubleMath.roundToInt(relevance * 10000, HALF_EVEN);
        } else {
            score = 100 + DoubleMath.roundToInt(relevance * 100, HALF_EVEN);
        }
        if (score == 0) {
            return;
        }

        final int boost = prefs.changeProposalRelevance ? score : 0;
        if (boost > 0) {
            proposal.setTag(RECOMMENDERS_SCORE, relevance * 100);
        }

        String label = null;
        if (prefs.decorateProposalText) {
            String format = relevance < 0.01d ? Messages.PROPOSAL_LABEL_PROMILLE : Messages.PROPOSAL_LABEL_PERCENTAGE;
            label = format(format, relevance);
        }

        ProposalProcessorManager manager = proposal.getProposalProcessorManager();

        if (boost != 0 || label != null) {
            manager.addProcessor(new SimpleProposalProcessor(boost, label));
        }

        if (prefs.decorateProposalIcon) {
            manager.addProcessor(overlayProcessor);
        }
    }

    private boolean isCompletionRequestSupported(IRecommendersCompletionContext context) {
        final ASTNode node = context.getCompletionNode().orNull();
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
}
