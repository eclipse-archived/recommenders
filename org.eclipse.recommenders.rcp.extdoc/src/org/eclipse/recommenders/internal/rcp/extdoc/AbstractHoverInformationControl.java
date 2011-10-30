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
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.internal.rcp.extdoc.UpdateService.AbstractUpdateJob;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiUpdateJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

abstract class AbstractHoverInformationControl extends AbstractInformationControl implements
        IInformationControlExtension2 {

    private final UiManager uiManager;
    private final ProviderStore providerStore;
    private final UpdateService updateService;
    private ProvidersComposite providersComposite;

    private IJavaElementSelection lastSelection;
    private Map<IProvider, IAction> actions;

    AbstractHoverInformationControl(final Shell parentShell, final UiManager uiManager,
            final ProviderStore providerStore, final UpdateService updateService,
            final AbstractHoverInformationControl copy) {
        super(parentShell, new ToolBarManager(SWT.FLAT));
        this.uiManager = uiManager;
        this.providerStore = providerStore;
        this.updateService = updateService;
        if (copy != null) {
            copyInformationControl(copy);
        }
        create();
    }

    private void copyInformationControl(final AbstractHoverInformationControl copy) {
        providersComposite = copy.providersComposite;
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
        if (providersComposite == null) {
            createContentControl(parent);
            fillToolbar(getToolBarManager());
        } else {
            providersComposite.setParent(parent);
        }
    }

    private void createContentControl(final Composite parent) {
        final Option<IWorkbenchPartSite> site = uiManager.getWorkbenchSite();
        if (site.hasValue()) {
            providersComposite = new ProvidersComposite(parent, site.get().getWorkbenchWindow());
        }
    }

    private void fillToolbar(final ToolBarManager toolbar) {
        addProviderActions(toolbar);
        toolbar.add(new Separator());
        addOpenInputAction(toolbar);
        addShowInViewAction(toolbar);
        toolbar.update(true);
    }

    private void addProviderActions(final ToolBarManager toolbar) {
        actions = new HashMap<IProvider, IAction>();
        for (final IProvider provider : providerStore.getProviders()) {
            final Composite providerComposite = providersComposite.addProvider(provider);
            final IAction action = new Action("Scroll to " + provider.getProviderFullName(),
                    ImageDescriptor.createFromImage(provider.getIcon())) {
                @Override
                public void run() {
                    providersComposite.scrollToProvider(providerComposite);
                }
            };
            toolbar.add(action);
            actions.put(provider, action);
        }
    }

    private void addOpenInputAction(final ToolBarManager toolbar) {
        toolbar.add(new Action("Open Input", ImageDescriptor.createFromImage(ExtDocPlugin
                .getIcon("lcl16/goto_input.png"))) {
            @Override
            public void run() {
                final Option<IWorkbenchPartSite> site = uiManager.getWorkbenchSite();
                if (site.hasValue()) {
                    new OpenAction(site.get()).run(new Object[] { lastSelection.getJavaElement() });
                }
            }
        });
    }

    private void addShowInViewAction(final ToolBarManager toolbar) {
        toolbar.add(new Action("Show in ExtDoc View", ExtDocPlugin.getIconDescriptor("lcl16/extdoc_open.png")) {
            @Override
            public void run() {
                uiManager.selectionChanged(lastSelection);
            }
        });
    }

    @Override
    public void setInput(final Object input) {
        final IJavaElementSelection selection = getSelection(input);
        if (!selection.equals(lastSelection)) {
            lastSelection = selection;
            updateProviders();
            providersComposite.updateSelectionLabel(selection.getJavaElement());
        }
    }

    private void updateProviders() {
        for (final Composite control : providersComposite.getProviders()) {
            ((GridData) control.getLayoutData()).exclude = true;
            actions.get(control.getData()).setEnabled(false);
            updateService.schedule(new PopUpProviderUpdateJob(control));
        }
        updateService.invokeAll();
    }

    UiManager getUiManager() {
        return uiManager;
    }

    ProviderStore getProviderStore() {
        return providerStore;
    }

    UpdateService getUpdateService() {
        return updateService;
    }

    abstract IJavaElementSelection getSelection(Object object);

    private final class PopUpProviderUpdateJob extends AbstractUpdateJob {

        private final Composite control;
        private final IProvider provider;

        private boolean displayProviderOnSuccess;

        PopUpProviderUpdateJob(final Composite control) {
            this.control = control;
            provider = (IProvider) control.getData();
        }

        @Override
        public void run() {
            try {
                displayProviderOnSuccess = lastSelection != null && provider.selectionChanged(lastSelection, control);
            } catch (final Exception e) {
                ExtDocPlugin.logException(e);
            }
        }

        @Override
        public void finishSuccessful() {
            if (displayProviderOnSuccess) {
                displayProvider();
            }
        }

        @Override
        public void handleTimeout() {
            if (!provider.hideOnTimeout()) {
                displayTimeoutMessage(provider.resolveContentComposite(control));
                displayProvider();
            }
        }

        private void displayProvider() {
            ProviderUiUpdateJob.run(new ProviderUiUpdateJob() {
                @Override
                public void run(final Composite composite) {
                    ((GridData) composite.getLayoutData()).exclude = false;
                    actions.get(provider).setEnabled(true);
                }
            }, control);
        }

    }
}
