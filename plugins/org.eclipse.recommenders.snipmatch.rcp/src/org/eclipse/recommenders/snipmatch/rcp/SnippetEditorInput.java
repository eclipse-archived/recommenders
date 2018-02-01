/**
 * Copyright (c) 2013 Stefan Prisca.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Prisca - initial API and implementation
 *     Olav Lenz - change to fileless approach.
 */
package org.eclipse.recommenders.snipmatch.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class SnippetEditorInput implements IEditorInput {

    private final Snippet snippet;
    private ISnippet oldSnippet;
    private ISnippetRepository snippetRepository;

    public SnippetEditorInput(ISnippet snippet) {
        this(snippet, null);
    }

    public SnippetEditorInput(ISnippet snippet, ISnippetRepository snippetRepository) {
        this.oldSnippet = snippet;
        this.snippet = Snippet.copy(snippet);
        this.snippetRepository = snippetRepository;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public ISnippet getOldSnippet() {
        return oldSnippet;
    }

    public ISnippetRepository getRepository() {
        return snippetRepository;
    }

    public void setRepository(ISnippetRepository repository) {
        snippetRepository = repository;
    }

    @Override
    public boolean exists() {
        return true;
    }

    // This is needed to avoid having two different editors opened for the same file
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SnippetEditorInput) {
            SnippetEditorInput other = (SnippetEditorInput) obj;
            return getOldSnippet().equals(other.getOldSnippet());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getOldSnippet().hashCode();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return snippet.getName();
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return snippet.getName() + " - " + snippet.getDescription(); //$NON-NLS-1$
    }

    public void setOldSnippet(Snippet oldSnippet) {
        this.oldSnippet = oldSnippet;
    }
}
