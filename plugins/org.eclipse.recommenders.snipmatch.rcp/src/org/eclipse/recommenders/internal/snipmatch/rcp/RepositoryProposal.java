/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import java.text.MessageFormat;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class RepositoryProposal implements ICompletionProposal, ICompletionProposalExtension6 {

    private final String name;
    private final int matches;
    private final int repositoryPriority;

    public RepositoryProposal(SnippetRepositoryConfiguration newRepository, int repositoryPriority, int matches) {
        this.name = newRepository.getName();
        this.matches = matches;
        this.repositoryPriority = repositoryPriority;
    }

    @Override
    public StyledString getStyledDisplayString() {
        StyledString styledString = new StyledString();
        styledString.append("--- " + name + " ---", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$ //$NON-NLS-2$
        styledString.append(" "); //$NON-NLS-1$
        styledString.append(MessageFormat.format(Messages.COMPLETION_ENGINE_REPOSITORY_MATCHES, name, matches),
                StyledString.COUNTER_STYLER);
        return styledString;
    }

    @Override
    public String getDisplayString() {
        return getStyledDisplayString().toString();
    }

    public String getName() {
        return name;
    }

    public int getNumberOfMatches() {
        return matches;
    }

    public int getRepositoryPriority() {
        return repositoryPriority;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public Point getSelection(IDocument document) {
        return null;
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    @Override
    public void apply(IDocument document) {
        // no-op
    }

    @Override
    public String getAdditionalProposalInfo() {
        return null;
    }
}
