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
package org.eclipse.recommenders.completion.rcp;

import java.util.List;

import org.eclipse.jdt.internal.ui.text.java.ProposalSorterRegistry;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.utils.annotations.Provisional;

@Provisional
public abstract class SessionProcessor {

    /**
     * called after a new completion session was started. The given context already contains the initial jdt proposal.
     * SessionProcessors may add additional proposals here if required.
     */
    public void startSession(IRecommendersCompletionContext context) {
    }

    /**
     * Called for every {@link IProcessableProposal} to allow adding individual {@link ProposalProcessor}s to a
     * proposal.
     */
    public void process(IProcessableProposal proposal) throws Exception {
    }

    /**
     * Called when the proposal computation is done. Processors may add additional proposal now if required. But be
     * careful not interfere badly with other processors.
     */
    public void endSession(List<ICompletionProposal> proposals) {
    }

    /**
     * Presents the final list of proposals to all interested parties. This list is not yet ordered as the final
     * ordering is determined by the {@link ProposalSorterRegistry#getCurrentSorter()} as part of the UI.
     * 
     * @param proposals
     *            the final but unordered list
     */
    public void aboutToShow(List<ICompletionProposal> proposals) {
    }

    /**
     * Called whenever a proposal was selected in the ui. Note that this method may be called repeatedly with the same
     * proposal.
     */
    public void selected(ICompletionProposal proposal) {
    }

    /**
     * Called when a proposal was applied on the code.
     */
    public void applied(ICompletionProposal proposal) {
    }

}
