/**
 * Copyright (c) 2013 Stefan Prisca.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Prisca - initial API and implementation
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import static java.util.UUID.nameUUIDFromBytes;
import static org.eclipse.recommenders.utils.Checks.ensureIsInstanceOf;

import java.io.IOException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnippetEditor extends FormEditor implements IResourceChangeListener {

    private static Logger LOG = LoggerFactory.getLogger(SnippetEditor.class);
    private boolean dirty;

    private SnippetMetadataPage metadataEditorPage;
    private SnippetSourcePage sourceEditorPage;

    public SnippetEditor() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        ensureIsInstanceOf(editorInput, SnippetEditorInput.class);
        setPartName(editorInput.getName());
        super.init(site, editorInput);
    }

    @Override
    protected void addPages() {
        try {
            metadataEditorPage = new SnippetMetadataPage(this, "meta", "Metadata");
            addPage(metadataEditorPage);
            sourceEditorPage = new SnippetSourcePage(this, "source", "Snippet Source");
            addPage(sourceEditorPage);
        } catch (PartInitException e) {
            LOG.error("Exception while adding editor pages.", e);
        }
    }

    public void setDirty(boolean newDirty) {
        if (dirty != newDirty) {
            dirty = newDirty;
            editorDirtyStateChanged();
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        SnippetEditorInput input = (SnippetEditorInput) getEditorInput();

        Snippet snippet = (Snippet) input.getSnippet();
        ISnippetRepository repo = input.getRepository();

        if (repo == null) {
            MessageDialog.openError(getSite().getShell(), "Error while storing snippet.",
                    "No repository available to store changed snippet.");
            return;
        }

        ISnippet oldSnippet = input.getOldSnippet();

        if (!snippet.getCode().equals(oldSnippet.getCode())) {
            int status = new MessageDialog(
                    getSite().getShell(),
                    "Save snippet.",
                    null,
                    "You changed the snippet’s source. Do you want to overwrite the original snippet or store your changes as a new snippet?",
                    MessageDialog.QUESTION, new String[] { "Overwrite", "Store as New", "Cancel" }, 0).open();

            if (status == 1) {
                // Store as new
                snippet.setUUID(nameUUIDFromBytes(snippet.getCode().getBytes()));
                setInput(new SnippetEditorInput(snippet, input.getRepository()));
                updateEditorPages();
            }

            if (status == 2) {
                // Cancel
                return;
            }
        }

        try {
            repo.importSnippet(snippet);
            setPartName(getEditorInput().getName());
            setDirty(false);
        } catch (IOException e) {
            LOG.error("Exception while storing snippet.", e);
        }
    }

    private void updateEditorPages() {
        metadataEditorPage.init(getEditorSite(), getEditorInput());
        sourceEditorPage.init(getEditorSite(), getEditorInput());
        metadataEditorPage.update();
        sourceEditorPage.update();
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
    }
}
