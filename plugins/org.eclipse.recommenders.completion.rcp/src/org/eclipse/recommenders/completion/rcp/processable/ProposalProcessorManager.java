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

import java.util.Set;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;

@Beta
public class ProposalProcessorManager {

    private final Set<ProposalProcessor> processors = Sets.newLinkedHashSet();
    private final IProcessableProposal proposal;
    private final StyledString initialDisplayString;

    @SuppressWarnings("unused")
    // intended for debugging purpose
    private final int initialRelevance;

    public ProposalProcessorManager(IProcessableProposal proposal) {
        this.proposal = proposal;
        initialRelevance = proposal.getRelevance();
        initialDisplayString = deepCopy(proposal.getStyledDisplayString());
    }

    public void addProcessor(ProposalProcessor processor) {
        processors.add(processor);
    }

    public boolean prefixChanged(String prefix) {
        boolean keepProposal = false;
        StyledString tmpStyledString = deepCopy(initialDisplayString);
        int tmpRelevance = 0;

        for (ProposalProcessor p : processors) {
            keepProposal |= p.isPrefix(prefix);
            p.modifyDisplayString(tmpStyledString);
            tmpRelevance += p.modifyRelevance();
        }
        proposal.setRelevance(tmpRelevance);
        proposal.setStyledDisplayString(tmpStyledString);
        return keepProposal;
    }

    public static StyledString deepCopy(final StyledString displayString) {
        final StyledString copy = new StyledString(displayString.getString());
        for (final StyleRange range : displayString.getStyleRanges()) {
            copy.setStyle(range.start, range.length, new Styler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.background = range.background;
                    textStyle.borderColor = range.borderColor;
                    textStyle.borderStyle = range.borderStyle;
                    textStyle.font = range.font;
                    textStyle.foreground = range.foreground;
                }
            });
        }
        return copy;
    }
}
