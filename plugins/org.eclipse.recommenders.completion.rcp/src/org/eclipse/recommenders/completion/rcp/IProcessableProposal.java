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

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.utils.annotations.Provisional;

import com.google.common.base.Optional;

@Provisional
public interface IProcessableProposal extends IJavaCompletionProposal {

    void setRelevance(int newRelevance);

    StyledString getStyledDisplayString();

    void setStyledDisplayString(StyledString styledDisplayString);

    ProposalProcessorManager getProposalProcessorManager();

    void setProposalProcessorManager(ProposalProcessorManager mgr);

    Optional<CompletionProposal> getCoreProposal();

    // boolean isPrefix(String prefix, String completion);
    String getPrefix();

}
