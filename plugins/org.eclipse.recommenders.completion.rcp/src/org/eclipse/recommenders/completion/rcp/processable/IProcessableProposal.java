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

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

@Beta
public interface IProcessableProposal extends IJavaCompletionProposal {

    void setRelevance(int newRelevance);

    void setImage(Image image);

    StyledString getStyledDisplayString();

    void setStyledDisplayString(StyledString styledDisplayString);

    ProposalProcessorManager getProposalProcessorManager();

    void setProposalProcessorManager(ProposalProcessorManager mgr);

    Optional<CompletionProposal> getCoreProposal();

    String getPrefix();

    void setTag(IProposalTag key, Object value);

    <T> Optional<T> getTag(IProposalTag key);

    <T> T getTag(IProposalTag key, T defaultValue);
}
