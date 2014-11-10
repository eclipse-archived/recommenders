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
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class RepositoryProposal implements ICompletionProposal {

    private final String name;
    private final int matches;
    private final int repositoryPriority;

    public RepositoryProposal(SnippetRepositoryConfiguration newRepository, int repositoryPriority, int matches) {
        this.name = newRepository.getName();
        this.matches = matches;
        this.repositoryPriority = repositoryPriority;
    }

    @Override
    public String getDisplayString() {
        return MessageFormat.format(Messages.COMPLETION_ENGINE_REPOSITORY_MATCHES, name, matches);
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
    public Point getSelection(IDocument document) {
        return null;
    }

    @Override
    public Image getImage() {
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
