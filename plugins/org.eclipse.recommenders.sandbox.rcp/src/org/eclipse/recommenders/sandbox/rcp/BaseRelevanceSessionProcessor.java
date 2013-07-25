/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.sandbox.rcp;

import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;

public class BaseRelevanceSessionProcessor extends SessionProcessor {

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
        int relevance = proposal.getRelevance();
        mgr.addProcessor(new SimpleProposalProcessor(relevance));
    }
}
