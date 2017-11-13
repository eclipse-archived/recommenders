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
package org.eclipse.recommenders.internal.mylyn.rcp;

import static java.lang.Math.round;
import static java.text.MessageFormat.format;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContextManager;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.internal.mylyn.rcp.l10n.Messages;

@SuppressWarnings("restriction")
public class MylynSessionProcessor extends SessionProcessor {

    private IInteractionContextManager contextManager;
    private float minInterest;

    @Override
    public boolean startSession(IRecommendersCompletionContext crContext) {
        contextManager = ContextCore.getContextManager();
        minInterest = ContextCore.getCommonContextScaling().getInteresting();
        return contextManager.isContextActive();
    }

    @Override
    public void process(IProcessableProposal p) throws Exception {
        if (!(p instanceof AbstractJavaCompletionProposal)) {
            return;
        }
        AbstractJavaCompletionProposal proposal = (AbstractJavaCompletionProposal) p;
        IJavaElement javaElement = proposal.getJavaElement();
        if (javaElement == null) {
            return;
        }
        String handle = javaElement.getHandleIdentifier();
        IInteractionElement interactionElement = contextManager.getElement(handle);
        if (interactionElement == null) {
            return;
        }
        float interest = interactionElement.getInterest().getValue();
        if (interest > minInterest) {
            String label = format(Messages.PROPOSAL_LABEL_MYLYN_INTEREST, interest);
            ProposalProcessorManager proposalMgr = p.getProposalProcessorManager();
            SimpleProposalProcessor processor = new SimpleProposalProcessor(round(interest), label);
            proposalMgr.addProcessor(processor);
        }
    }

    @Override
    public void endSession(List<ICompletionProposal> proposals) {
        contextManager = null;
    }
}
