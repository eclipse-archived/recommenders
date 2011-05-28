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
package org.eclipse.recommenders.internal.rcp.extdoc.views;

import java.util.Map.Entry;

import com.google.inject.Inject;

import org.eclipse.recommenders.commons.selection.ExtendedSelectionContext;
import org.eclipse.recommenders.commons.selection.SelectionPlugin;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

public final class ExtDocView extends ViewPart {

    private final ProviderStore providerStore;
    private CTabFolder folder;
    private ExtendedSelectionContext lastContext;

    @Inject
    public ExtDocView(final ProviderStore providerStore) {
        this.providerStore = providerStore;
    }

    public void update(final ExtendedSelectionContext context) {
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

        for (final Entry<String, IProvider> provider : providerStore.getProviders().entrySet()) {
            final CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(provider.getKey());
            final Control control = provider.getValue().createControl(folder, getViewSite());
            item.setControl(control);
        }
        folder.setSelection(0);
        folder.addSelectionListener(new Listener());

        final IWorkbenchPage page = getSite().getPage();
        SelectionPlugin.triggerUpdate(page.getActivePart(), page.getSelection());
    }

    @Override
    public void setFocus() {
        folder.setFocus();
    }

    private class Listener implements org.eclipse.swt.events.SelectionListener {

        @Override
        public void widgetSelected(final SelectionEvent event) {
            update(lastContext);
        }

        @Override
        public void widgetDefaultSelected(final SelectionEvent e) {
        }

    }
}
