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
package org.eclipse.recommenders.completion.rcp.processable;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.jface.viewers.StyledString.COUNTER_STYLER;

import java.util.Objects;

import org.eclipse.jface.viewers.StyledString;

import com.google.common.annotations.VisibleForTesting;

public class SimpleProposalProcessor extends ProposalProcessor {

    private final int increment;
    private final String addon;

    public SimpleProposalProcessor(int increment, String addon) {
        this.increment = increment;
        this.addon = addon;
    }

    public SimpleProposalProcessor(int increment) {
        this(increment, null);
    }

    @Override
    public int modifyRelevance() {
        return increment;
    }

    @Override
    public void modifyDisplayString(StyledString displayString) {
        if (!isEmpty(addon)) {
            displayString.append(" - " + addon, COUNTER_STYLER); //$NON-NLS-1$
        }
    }

    @VisibleForTesting
    public int getIncrement() {
        return increment;
    }

    @VisibleForTesting
    public String getAddon() {
        return addon;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        SimpleProposalProcessor that = (SimpleProposalProcessor) other;
        return this.increment == that.increment && Objects.equals(this.addon, that.addon);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (addon == null ? 0 : addon.hashCode());
        result = prime * result + increment;
        return result;
    }

    @Override
    public String toString() {
        return String.format("'%s' -> %+d", addon, increment);
    }
}
