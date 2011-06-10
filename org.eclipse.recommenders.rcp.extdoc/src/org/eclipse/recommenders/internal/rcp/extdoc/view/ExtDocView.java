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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("restriction")
public final class ExtDocView extends ViewPart {

    private final ProviderStore providerStore;
    private IJavaElementSelection lastContext;

    private ProvidersComposite providersComposite;
    private ProvidersTable table;

    @Inject
    public ExtDocView(final ProviderStore providerStore) {
        this.providerStore = providerStore;
    }

    public void update(final IJavaElementSelection context) {
        if (context != null && table != null) {
            for (final TableItem item : table.getItems()) {
                if (item.getChecked()) {
                    final Control providerControl = (Control) item.getData();
                    ((IProvider) providerControl.getData()).selectionChanged(context);
                }
            }
            lastContext = context;
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        final SashForm sashForm = new SashForm(parent, SWT.SMOOTH);
        table = new ProvidersTable(sashForm, SWT.CHECK | SWT.FULL_SELECTION);
        providersComposite = new ProvidersComposite(sashForm, SWT.NONE);
        sashForm.setWeights(new int[] { 15, 85 });

        addProviders();
        fillActionBars();
    }

    private void addProviders() {
        for (final Tuple<String, IProvider> provider : providerStore.getProviders()) {
            final Control control = provider.getSecond().createControl(providersComposite, getViewSite());
            control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            control.setData(provider.getSecond());
            table.addProvider(control, provider.getFirst(), "eview16/star.png", true);
        }
    }

    @Override
    public void setFocus() {
        Preconditions.checkArgument(providersComposite.setFocus());
    }

    private void fillActionBars() {
        final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        toolbar.removeAll();
        toolbar.add(new FeedbackAction());
    }

    private final class FeedbackAction extends Action {

        FeedbackAction() {
            JavaPluginImages.setLocalImageDescriptors(this, "feedback.png");
        }

        @Override
        public void run() {
        }
    }
}
