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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import static java.lang.Math.rint;
import static org.eclipse.recommenders.internal.completion.rcp.ProcessableCompletionProposalComputer.NULL_PROPOSAL;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.ProposalProcessor;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.inject.Inject;

public class OverridesSessionProcessor extends SessionProcessor {

    private List<OverridesRecommendation> recommendations;

    private OverridesRecommender recommender;

    @Inject
    public OverridesSessionProcessor(OverridesRecommender recommender) {
        this.recommender = recommender;
    }

    @Override
    public void startSession(IRecommendersCompletionContext context) {
        try {
            recommender.startSession(context);
            recommendations = recommender.getRecommendations();
        } catch (CoreException e) {
            RecommendersPlugin.log(e);
        }
    }

    @Override
    public void process(IProcessableProposal proposal) {
        CompletionProposal cProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (cProposal.getKind()) {
        case CompletionProposal.METHOD_DECLARATION:
            final String signature = String.valueOf(cProposal.getSignature()).replace('.', '/');
            final String name = String.valueOf(cProposal.getName());
            final String propSignature = (name + signature).replaceAll("<\\.>", "");
            for (final OverridesRecommendation r : recommendations) {
                final String recSignature = r.method.getSignature();
                if (recSignature.equals(propSignature)) {
                    final int percentage = (int) rint(r.probability * 100);
                    proposal.getProposalProcessorManager().addProcessor(new ProposalProcessor() {

                        @Override
                        public void modifyRelevance(AtomicInteger relevance) {
                            int increment = 100 + percentage;
                            relevance.addAndGet(increment);
                        }

                        @Override
                        public void modifyDisplayString(StyledString displayString) {
                            displayString.append(" - " + percentage + "%", StyledString.COUNTER_STYLER);
                        }
                    });
                }
            }
        }
    }
}
