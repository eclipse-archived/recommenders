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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import org.eclipse.recommenders.commons.internal.selection.SelectionPlugin;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("restriction")
final class ExtDocView extends ViewPart {

    private final ProviderStore providerStore;
    private CTabFolder folder;
    private IJavaElementSelection lastContext;

    @Inject
    public ExtDocView(final ProviderStore providerStore) {
        this.providerStore = providerStore;
    }

    void update(final IJavaElementSelection context) {
        if (context != null && folder != null) {
            final CTabItem tabItem = folder.getSelection();
            providerStore.getProvider(tabItem.getText()).selectionChanged(context);
            lastContext = context;
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        folder = new CTabFolder(parent, SWT.BOTTOM);
        folder.setSimple(false);
        addProviderTabs();
        folder.setSelection(0);
        folder.addSelectionListener(new Listener());

        final IWorkbenchPage page = getSite().getPage();
        SelectionPlugin.triggerUpdate(page.getActivePart(), page.getSelection());
    }

    private void addProviderTabs() {
        for (final Tuple<String, IProvider> provider : providerStore.getProviders()) {
            final CTabItem item = new CTabItem(folder, SWT.NONE);
            final Control control = provider.getSecond().createControl(folder, getViewSite());
            item.setText(provider.getFirst());
            item.setControl(control);
        }
    }

    @Override
    public void setFocus() {
        Preconditions.checkArgument(folder.setFocus());
    }

    private final class Listener implements org.eclipse.swt.events.SelectionListener {

        @Override
        public void widgetSelected(final SelectionEvent event) {
            update(lastContext);
        }

        @Override
        public void widgetDefaultSelected(final SelectionEvent e) {
            // Not of interest to us.
        }

    }
}
