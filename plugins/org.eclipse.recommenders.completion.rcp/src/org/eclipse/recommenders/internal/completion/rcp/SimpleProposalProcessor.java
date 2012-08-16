package org.eclipse.recommenders.internal.completion.rcp;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.ProposalProcessor;

public class SimpleProposalProcessor extends ProposalProcessor {

    private int increment;
    private String addon;

    public SimpleProposalProcessor(int increment, String addon) {
        this.increment = increment;
        this.addon = addon;
    }

    public SimpleProposalProcessor(int increment) {
        this(increment, null);
    }

    @Override
    public void modifyRelevance(AtomicInteger relevance) {
        relevance.addAndGet(increment);
    }

    @Override
    public void modifyDisplayString(StyledString displayString) {
        if (addon != null) displayString.append(" - " + addon, StyledString.COUNTER_STYLER);
    }
}