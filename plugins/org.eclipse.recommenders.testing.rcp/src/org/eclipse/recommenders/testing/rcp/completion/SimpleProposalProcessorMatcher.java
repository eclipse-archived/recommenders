/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.testing.rcp.completion;

import static org.mockito.Matchers.argThat;

import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.mockito.ArgumentMatcher;

public final class SimpleProposalProcessorMatcher extends ArgumentMatcher<SimpleProposalProcessor> {

    private final int boost;
    private final String label;

    private SimpleProposalProcessorMatcher(Integer boost, String label) {
        this.boost = boost;
        this.label = label;
    }

    public static SimpleProposalProcessor processorWithBoost(int boost) {
        return argThat(new SimpleProposalProcessorMatcher(boost, null));
    }

    public static SimpleProposalProcessor processorWithBoostAndLabel(int boost, String label) {
        return argThat(new SimpleProposalProcessorMatcher(boost, label));
    }

    @Override
    public boolean matches(Object argument) {
        if (!(argument instanceof SimpleProposalProcessor)) {
            return false;
        }
        SimpleProposalProcessor processor = (SimpleProposalProcessor) argument;
        if (boost != processor.getIncrement()) {
            return false;
        }

        if (label == null) {
            return label == processor.getAddon();
        }

        return label.equals(processor.getAddon());
    }
}
