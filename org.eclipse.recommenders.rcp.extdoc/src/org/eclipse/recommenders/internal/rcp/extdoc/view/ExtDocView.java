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
package org.eclipse.recommenders.internal.rcp.extdoc.view;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.internal.rcp.extdoc.ProvidersComposite;
import org.eclipse.recommenders.internal.rcp.extdoc.UpdateService;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;

import com.google.inject.Inject;

/**
 * Displays provider content as well as table for selecting and ordering
 * providers in a view.
 */
public class ExtDocView extends ViewPart {

    public static final int HEAD_LABEL_HEIGHT = 20;
    private static final String SASH_POSITION_KEY = "extDocSashPosition";

    private final ProviderStore providerStore;
    private final UpdateService updateService;

    private ProvidersComposite providersComposite;
    private ProvidersTable table;
    private boolean linkingEnabled = true;

    @Inject
    ExtDocView(final ProviderStore providerStore, final UpdateService updateService) {
        this.providerStore = providerStore;
        this.updateService = updateService;
    }

    @Override
    public final void createPartControl(final Composite parent) {
        createSash(parent);
        addProviders();
        fillActionBars();
    }

    private void createSash(final Composite parent) {
        final SashForm sashForm = new SashForm(parent, SWT.SMOOTH);
        sashForm.setLayout(new FillLayout());
        table = new ProvidersTable(sashForm, providerStore, updateService);
        providersComposite = new ProvidersComposite(sashForm, getViewSite().getWorkbenchWindow());
        table.setProvidersComposite(providersComposite);
        handleSashWeights(sashForm);
    }

    private static void handleSashWeights(final SashForm sashForm) {
        final int sashWeight = ExtDocPlugin.getPreferences().getInt(SASH_POSITION_KEY, 150);
        sashForm.setWeights(new int[] { sashWeight, 1000 - sashWeight });
        sashForm.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent event) {
                ExtDocPlugin.getPreferences().putInt(SASH_POSITION_KEY, sashForm.getWeights()[0]);
            }
        });
    }

    private void addProviders() {
        for (final IProvider provider : providerStore.getProviders()) {
            final Composite composite = providersComposite.addProvider(provider);
            table.addProvider(composite, provider.getProviderName(), provider.getIcon());
        }
    }

    private void fillActionBars() {
        final IViewSite viewSite = getViewSite();
        if (viewSite != null) {
            final IToolBarManager toolbar = viewSite.getActionBars().getToolBarManager();
            toolbar.removeAll();
            toolbar.add(new OpenInputAction());
            toolbar.add(new LinkWithEditorAction());
        }
    }

    /**
     * @param selection
     *            The current user selection which shall be passed to the
     *            providers.
     */
    public final void selectionChanged(final IJavaElementSelection selection) {
        if (selection != null && table != null) {
            table.setContext(selection);
            updateProviders(selection);
            providersComposite.scrollToTop();
            providersComposite.updateSelectionLabel(selection.getJavaElement());
        }
    }

    private void updateProviders(final IJavaElementSelection selection) {
        for (final TableItem item : table.getItems()) {
            if (item.getChecked()) {
                updateService.schedule(new ProviderUpdateJob(table, item, selection));
            }
        }
        updateService.invokeAll();
    }

    @Override
    public final void setFocus() {
        providersComposite.setFocus();
    }

    /**
     * @return True, if the view should be updated on new selections.
     */
    public final boolean isLinkingEnabled() {
        return linkingEnabled;
    }

    private final class OpenInputAction extends Action {

        OpenInputAction() {
            super("Link with Selection", SWT.TOGGLE);
            setImageDescriptor(ExtDocPlugin.getIconDescriptor("lcl16/goto_input.png"));
        }

        @Override
        public void run() {
            final IJavaElement inputElement = table.getLastSelection().getJavaElement();
            new OpenAction(getViewSite()).run(new Object[] { inputElement });
        }
    }

    private final class LinkWithEditorAction extends Action {

        LinkWithEditorAction() {
            super("Link with Selection", SWT.TOGGLE);
            setImageDescriptor(ExtDocPlugin.getIconDescriptor("lcl16/link.gif"));
            setChecked(true);
        }

        @Override
        public void run() {
            linkingEnabled ^= true;
        }
    }
}
