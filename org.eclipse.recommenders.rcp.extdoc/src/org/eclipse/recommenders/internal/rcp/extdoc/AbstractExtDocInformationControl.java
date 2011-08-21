/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

abstract class AbstractExtDocInformationControl extends AbstractInformationControl implements
        IInformationControlExtension2 {

    private final UiManager uiManager;
    private final ProviderStore providerStore;
    private ProvidersComposite composite;

    private IJavaElementSelection lastSelection;
    private Map<IProvider, IAction> actions;

    AbstractExtDocInformationControl(final Shell parentShell, final UiManager uiManager,
            final ProviderStore providerStore, final AbstractExtDocInformationControl copy) {
        super(parentShell, new ToolBarManager(SWT.FLAT));
        this.uiManager = uiManager;
        this.providerStore = providerStore;
        if (copy != null) {
            copyInformationControl(copy);
        }
        create();
    }

    private void copyInformationControl(final AbstractExtDocInformationControl copy) {
        composite = copy.composite;
        lastSelection = copy.lastSelection;
        actions = copy.actions;
        final ToolBarManager manager = getToolBarManager();
        for (final IContributionItem item : copy.getToolBarManager().getItems()) {
            if (item instanceof ActionContributionItem) {
                manager.add(((ActionContributionItem) item).getAction());
            } else {
                manager.add(item);
            }
        }
        manager.update(true);
    }

    @Override
    public final boolean hasContents() {
        return true;
    }

    @Override
    protected void createContent(final Composite parent) {
        if (composite == null) {
            createContentControl(parent);
            actions = new HashMap<IProvider, IAction>();
            fillToolbar(getToolBarManager());
        } else {
            composite.setParent(parent);
        }
    }

    private void createContentControl(final Composite parent) {
        composite = new ProvidersComposite(parent, uiManager.getWorkbenchSite().getWorkbenchWindow());
    }

    private void fillToolbar(final ToolBarManager toolbar) {
        addProviderActions(toolbar);
        toolbar.add(new Separator());
        toolbar.add(new Action("Open Input", ImageDescriptor.createFromImage(ExtDocPlugin
                .getIcon("lcl16/goto_input.png"))) {
            @Override
            public void run() {
                new OpenAction(uiManager.getWorkbenchSite()).run(new Object[] { lastSelection.getJavaElement() });
            }
        });
        toolbar.add(new Action("Show in ExtDoc View", ExtDocPlugin.getIconDescriptor("lcl16/extdoc_open.png")) {
            @Override
            public void run() {
                uiManager.selectionChanged(lastSelection);
            }
        });
        toolbar.update(true);
    }

    private void addProviderActions(final ToolBarManager toolbar) {
        for (final IProvider provider : providerStore.getProviders()) {
            final Composite providerComposite = composite.addProvider(provider);
            final IAction action = new Action("Scroll to " + provider.getProviderFullName(),
                    ImageDescriptor.createFromImage(provider.getIcon())) {
                @Override
                public void run() {
                    composite.scrollToProvider(providerComposite);
                }
            };
            toolbar.add(action);
            actions.put(provider, action);
        }
    }

    @Override
    public void setInput(final Object input) {
        final IJavaElementSelection selection = getSelection(input);
        if (!selection.equals(lastSelection)) {
            lastSelection = selection;
            for (final Composite control : composite.getProviders()) {
                ((GridData) control.getLayoutData()).exclude = true;
                actions.get(control.getData()).setEnabled(false);
                new ProviderJob(control).schedule();
            }
        }
    }

    UiManager getUiManager() {
        return uiManager;
    }

    ProviderStore getProviderStore() {
        return providerStore;
    }

    abstract IJavaElementSelection getSelection(Object object);

    private final class ProviderJob extends Job {

        private final Composite control;
        private final IProvider provider;

        ProviderJob(final Composite control) {
            super("Updating Hover Provider");
            this.control = control;
            provider = (IProvider) control.getData();
        }

        @Override
        public IStatus run(final IProgressMonitor monitor) {
            try {
                if (lastSelection != null && provider.selectionChanged(lastSelection, control)) {
                    ProviderUiJob.run(new ProviderUiJob() {
                        @Override
                        public void run(final Composite composite) {
                            ((GridData) composite.getLayoutData()).exclude = false;
                            actions.get(provider).setEnabled(true);
                        }
                    }, control);
                }
            } catch (final Exception e) {
                ExtDocPlugin.logException(e);
            }
            return Status.OK_STATUS;
        }

    }

}
