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
        scrolled = createScrolledComposite(sashForm);
        providersComposite = new ProvidersComposite(scrolled, SWT.NONE);
        scrolled.setContent(providersComposite);
        sashForm.setWeights(new int[] { 15, 85 });

        addProviders();
        fillActionBars();

        providersComposite.layout(true);
    }

    private ScrolledComposite createScrolledComposite(final Composite parent) {
        final ScrolledComposite composite = new ScrolledComposite(parent, SWT.V_SCROLL);
        composite.setExpandVertical(true);
        composite.setExpandHorizontal(true);
        composite.getVerticalBar().setIncrement(20);
        return composite;
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
            table.setContext(selection);
            for (final TableItem item : table.getItems()) {
                if (item.getChecked()) {
                    final IProvider provider = (IProvider) ((Control) item.getData()).getData();
                    new ProviderUpdateJob(item, provider, selection).schedule();
                }
            }
            scrolled.setOrigin(0, 0);
            new UIJob("Layout ExtDoc View") {
                @Override
                public IStatus runInUIThread(final IProgressMonitor monitor) {
                    providersComposite.layout(true);
                    scrolled.layout(true);
                    return Status.OK_STATUS;
                }
            }.schedule(1000);
        }
    }

    @Override
    public void setFocus() {
        scrolled.forceFocus();
    }

    private final class ProviderUpdateJob extends UIJob {

        private final TableItem item;
        private final IProvider provider;
        private final IJavaElementSelection selection;

        public ProviderUpdateJob(final TableItem item, final IProvider provider, final IJavaElementSelection selection) {
            super("Updating " + provider.getProviderFullName());
            super.setPriority(UIJob.SHORT);
            this.item = item;
            this.provider = provider;
            this.selection = selection;
        }

        @Override
        public IStatus runInUIThread(final IProgressMonitor monitor) {
            final boolean hasContent = provider.selectionChanged(selection);
            table.setContentVisible(item, hasContent);
            table.setGrayed(item, !hasContent);
            return Status.OK_STATUS;
        }
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
