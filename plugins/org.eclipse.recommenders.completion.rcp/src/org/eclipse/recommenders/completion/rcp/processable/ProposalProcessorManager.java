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
import org.eclipse.swt.graphics.Image;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;

@Beta
public class ProposalProcessorManager {

    private final Set<ProposalProcessor> processors = Sets.newLinkedHashSet();
    private final IProcessableProposal proposal;

    public ProposalProcessorManager(IProcessableProposal proposal) {
        this.proposal = proposal;
    }

    public void addProcessor(ProposalProcessor processor) {
        processors.add(processor);
    }

    public boolean prefixChanged(String prefix) {
        boolean keepProposal = false;
        int tmpRelevance = 0;

        for (ProposalProcessor p : processors) {
            keepProposal |= p.isPrefix(prefix);
            tmpRelevance += p.modifyRelevance();
        }
        proposal.setRelevance(tmpRelevance);
        return keepProposal;
    }

    public StyledString decorateStyledDisplayString(StyledString mutableStyledString) {
        for (ProposalProcessor p : processors) {
            p.modifyDisplayString(mutableStyledString);
        }
        return mutableStyledString;
    }

    public Image decorateImage(Image proposalImage) {
        Image img = proposalImage;
        for (ProposalProcessor p : processors) {
            img = p.modifyImage(img);
        }
        return img;
    }
}
