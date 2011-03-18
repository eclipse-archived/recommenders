/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.commons.codesearch;

public class Proposal {

    public static Proposal newProposal() {
        final Proposal proposal = new Proposal();
        return proposal;
    }

    public static Proposal newProposal(final float score, final SnippetSummary snippet) {
        final Proposal proposal = new Proposal();
        proposal.score = score;
        proposal.snippet = snippet;
        return proposal;
    }

    /**
     * The proposed code snippet.
     */
    public SnippetSummary snippet;

    /**
     * The overall score of this proposal.
     */
    public float score;

    // /**
    // * The individual feature scores. This is for debugging purpose and may be
    // * removed.
    // */
    // public FeatureWeights individualFeatureScores;

}
