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

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.google.common.annotations.Beta;

/**
 * ProposalProcessor can manipulate different aspects of a proposal such as prefix matching, display string and
 * relevance. They are typically registered at {@link ProposalProcessorManager#addProcessor(ProposalProcessor)} in
 * {@link SessionProcessor#process(IProcessableProposal)}.
 */
@Beta
public abstract class ProposalProcessor {

    /**
     * returns whether this prefix could work as prefix for this completion. This method is thought to enable other
     * matching strategies like subwords - or even more obscure token/proposal matchers.
     */
    public boolean isPrefix(String prefix) {
        return false;
    }

    /**
     * Enables processors to modify the given display string. It's always a fresh display string, but shared between all
     * processors.
     */
    public void modifyDisplayString(StyledString displayString) {
    }

    /**
     * used to update the default relevance of this proposal by some increment. The initial relevance value is JDT's
     * default value.
     * 
     * @return the sub-relevance
     */
    public int modifyRelevance() {
        return 0;
    }

    /**
     * Enables processors to decorate or completely replace the given image. The returned image will be used as input
     * for subsequent processors.
     */
    public Image modifyImage(Image image) {
        return image;
    }

}
