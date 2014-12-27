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
package org.eclipse.recommenders.snipmatch.rcp;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.eclipse.recommenders.snipmatch.Location.NONE;
import static org.eclipse.recommenders.utils.Checks.ensureIsInstanceOf;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.recommenders.internal.snipmatch.rcp.Constants;
import org.eclipse.recommenders.internal.snipmatch.rcp.LogMessages;
import org.eclipse.recommenders.internal.snipmatch.rcp.Messages;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories;
import org.eclipse.recommenders.internal.snipmatch.rcp.SelectRepositoryDialog;
import org.eclipse.recommenders.internal.snipmatch.rcp.editors.SnippetSourceValidator;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;

public class SnippetEditor extends FormEditor implements IResourceChangeListener {

    private static final int DEFAULT_PRIORITY = 100;

    private static Logger LOG = LoggerFactory.getLogger(SnippetEditor.class);

    private final Repositories repos;
    private final SnippetRepositoryConfigurations configs;

    @Inject
    public SnippetEditor(Repositories repos, SnippetRepositoryConfigurations configs) {
        this.repos = repos;
        this.configs = configs;

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        ensureIsInstanceOf(editorInput, SnippetEditorInput.class);
        setPartName(editorInput.getName());
        super.init(site, editorInput);
    }

    @Override
    public void setFocus() {
        super.setFocus();
        IFormPage activePage = getActivePageInstance();
        if (activePage == null) {
            return;
        }
        activePage.setFocus();
    }

    @Override
    protected void addPages() {
        try {
            for (IFormPage page : readExtensionPoint(this)) {
                addPage(page);
            }
        } catch (PartInitException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_LOAD_EDITOR_PAGE, e);
        }
    }

    private static List<IFormPage> readExtensionPoint(SnippetEditor editor) {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                Constants.EXT_POINT_PAGE_FACTORIES);

        List<IFormPage> pages = Lists.newLinkedList();
        for (final IConfigurationElement element : Ordering.natural()
                .onResultOf(new Function<IConfigurationElement, Integer>() {
                    @Override
                    public Integer apply(IConfigurationElement element) {
                        String priorityString = element.getAttribute("priority"); //$NON-NLS-1$
                        return priorityString == null ? DEFAULT_PRIORITY : Integer.parseInt(priorityString);
                    }
                }).sortedCopy(asList(elements))) {
            try {
                String id = element.getAttribute("id"); //$NON-NLS-1$
                String name = element.getAttribute("name"); //$NON-NLS-1$
                ISnippetEditorPageFactory pageFactory;
                pageFactory = (ISnippetEditorPageFactory) element.createExecutableExtension("class"); //$NON-NLS-1$
                IFormPage page = pageFactory.createPage(editor, id, name);
                pages.add(page);
            } catch (CoreException e) {
                continue;
            }
        }
        return pages;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        SnippetEditorInput input = (SnippetEditorInput) getEditorInput();

        Snippet snippet = input.getSnippet();

        if (isNullOrEmpty(snippet.getName())) {
            MessageDialog.openError(getSite().getShell(), Messages.DIALOG_TITLE_INAVLID_SNIPPET_NAME,
                    Messages.DIALOG_MESSAGE_INVALID_SNIPPET_NAME);
            monitor.setCanceled(true);
            return;
        }

        if (snippet.getLocation() == null || snippet.getLocation() == NONE) {
            MessageDialog.openError(getSite().getShell(), Messages.DIALOG_TITLE_INVALID_SNIPPET_LOCATION,
                    Messages.DIALOG_MESSAGE_INVALID_SNIPPET_LOCATION);
            monitor.setCanceled(true);
            return;
        }

        String sourceValid = SnippetSourceValidator.isSourceValid(snippet.getCode());
        if (!sourceValid.isEmpty()) {
            MessageDialog.openError(getSite().getShell(), Messages.DIALOG_TITLE_ERROR_SNIPPET_SOURCE_INVALID,
                    MessageFormat.format(Messages.DIALOG_MESSAGE_ERROR_SNIPPET_SOURCE_INVALID, sourceValid));
            monitor.setCanceled(true);
            return;
        }

        ISnippetRepository repo = input.getRepository();

        if (repo == null) {
            repo = SelectRepositoryDialog.openSelectRepositoryDialog(getSite().getShell(), repos, configs).orNull();
            if (repo == null) {
                return;
            }
            input.setRepository(repo);
        }

        ISnippet oldSnippet = input.getOldSnippet();

        if (!oldSnippet.getCode().isEmpty() && !snippet.getCode().equals(oldSnippet.getCode())) {
            int status = new MessageDialog(getSite().getShell(), Messages.DIALOG_TITLE_SAVE_SNIPPET, null,
                    Messages.DIALOG_MESSAGE_SAVE_SNIPPET_WITH_MODIFIED_CODE, MessageDialog.QUESTION, new String[] {
                            Messages.DIALOG_OPTION_SAVE, Messages.DIALOG_OPTION_SAVE_AS_NEW,
                            Messages.DIALOG_OPTION_CANCEL }, 0).open();

            if (status == 1) {
                // Store as new
                snippet.setUUID(randomUUID());
                setInputWithNotify(new SnippetEditorInput(snippet, input.getRepository()));
            }

            if (status == 2) {
                // Explicit Cancel
                monitor.setCanceled(true);
                return;
            }

            if (status == SWT.DEFAULT) {
                // Dialog closed => implicit Cancel
                monitor.setCanceled(true);
                return;
            }
        }

        try {
            commitPages(true);
            input.setOldSnippet(Snippet.copy(snippet));
            repo.importSnippet(snippet);
            setPartName(getEditorInput().getName());
            editorDirtyStateChanged();
        } catch (IOException e) {
            LOG.error("Exception while storing snippet.", e); //$NON-NLS-1$
        }
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
    }

    public void markDirtyUponSnippetCreation() {
        for (Object page : pages) {
            if (page instanceof IFormPage && ((IFormPage) page).getManagedForm() != null) {
                for (IFormPart part : ((IFormPage) page).getManagedForm().getParts()) {
                    if (part instanceof AbstractFormPart) {
                        ((AbstractFormPart) part).markDirty();
                    }
                }
            }
        }
    }

}
