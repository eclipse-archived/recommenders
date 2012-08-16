/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
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