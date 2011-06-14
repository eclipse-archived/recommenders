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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

@SuppressWarnings("restriction")
public final class ExtDocView extends ViewPart {

    private final ProviderStore providerStore;

    private ScrolledComposite scrolled;
    private ProvidersComposite providersComposite;
    private ProvidersTable table;

    @Inject
    public ExtDocView(final ProviderStore providerStore) {
        this.providerStore = providerStore;
    }

    @Override
    public void createPartControl(final Composite parent) {
        final SashForm sashForm = new SashForm(parent, SWT.SMOOTH);
        sashForm.setLayout(new FillLayout());
        table = new ProvidersTable(sashForm, SWT.CHECK | SWT.FULL_SELECTION);
        scrolled = new ScrolledComposite(sashForm, SWT.V_SCROLL);
        scrolled.setExpandVertical(true);
        scrolled.setExpandHorizontal(true);
        scrolled.setShowFocusedControl(true);
        providersComposite = new ProvidersComposite(scrolled, SWT.NONE);
        scrolled.setContent(providersComposite);
        sashForm.setWeights(new int[] { 15, 85 });

        addProviders();
        fillActionBars();

        providersComposite.layout(true);
    }

    private void addProviders() {
        for (final IProvider provider : providerStore.getProviders()) {
            final Control control = provider.createControl(providersComposite, getViewSite());
            control.setData(provider);
            table.addProvider(control, provider.getProviderName(), provider.getIcon(), true);
        }
    }

    private void fillActionBars() {
        final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        toolbar.removeAll();
        toolbar.add(new FeedbackAction());
    }

    public void update(final IJavaElementSelection selection) {
        if (selection != null && table != null) {
            table.setContext(selection.getElementLocation());
            for (final TableItem item : table.getItems()) {
                if (item.getChecked()) {
                    new UIJob("Provider Update") {
                        @Override
                        public IStatus runInUIThread(final IProgressMonitor monitor) {
                            final IProvider provider = (IProvider) ((Control) item.getData()).getData();
                            final boolean hasContent = provider.selectionChanged(selection);
                            table.setGrayed(item, !hasContent);
                            scrolled.layout(true);
                            scrolled.setMinHeight(providersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
                            return Status.OK_STATUS;
                        }
                    }.schedule();
                }
            }
        }
    }

    @Override
    public void setFocus() {
        Preconditions.checkArgument(scrolled.setFocus());
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
