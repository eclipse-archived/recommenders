package org.eclipse.recommenders.tests.completion.rcp.chain;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposalComputer;

public class TestingChainCompletionProposalComputer extends ChainCompletionProposalComputer {

    public TestingChainCompletionProposalComputer(IRecommendersCompletionContextFactory ctxFactory,
            IPreferenceStore preferenceStore) {
        super(ctxFactory, preferenceStore);
    }

    @Override
    protected boolean shouldMakeProposals() {
        return true;
    }

}
