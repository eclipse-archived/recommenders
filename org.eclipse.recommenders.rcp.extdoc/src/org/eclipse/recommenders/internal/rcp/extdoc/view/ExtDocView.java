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
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ExtDocView extends ViewPart {

    private final ProviderStore providerStore;

    private ScrolledComposite scrolled;
    private ProvidersComposite providersComposite;
    private ProvidersTable table;
    private CLabel selectionLabel;
    private JavaElementLabelProvider labelProvider;

    @Inject
    public ExtDocView(final ProviderStore providerStore) {
        this.providerStore = providerStore;
        initializeLabelProvider();
    }

    private void initializeLabelProvider() {
        labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_QUALIFIED
                | JavaElementLabelProvider.SHOW_OVERLAY_ICONS | JavaElementLabelProvider.SHOW_RETURN_TYPE
                | JavaElementLabelProvider.SHOW_PARAMETERS);
    }

    @Override
    public void createPartControl(final Composite parent) {
        createSash(parent);
        addProviders();
        fillActionBars();
    }

    private void createSash(final Composite parent) {
        final SashForm sashForm = new SashForm(parent, SWT.SMOOTH);
        sashForm.setLayout(new FillLayout());
        createLeftSashSide(sashForm);
        createRightSashSide(sashForm);
        sashForm.setWeights(new int[] { 15, 85 });
    }

    private void createLeftSashSide(final SashForm sashForm) {
        table = new ProvidersTable(sashForm, SWT.CHECK | SWT.FULL_SELECTION);
    }

    private void createRightSashSide(final SashForm sashForm) {
        final Composite container = SwtFactory.createGridComposite(sashForm, 1, 0, 0, 0, 0);
        createSelectionLabel(container);

        scrolled = createScrolledComposite(container);
        scrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        providersComposite = new ProvidersComposite(scrolled, SWT.NONE);
        scrolled.setContent(providersComposite);
        providersComposite.layout();
    }

    private void createSelectionLabel(final Composite container) {
        selectionLabel = new CLabel(container, SWT.NONE);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gridData.heightHint = 20;
        selectionLabel.setLayoutData(gridData);
        selectionLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
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
                    final ProviderUpdateJob job = new ProviderUpdateJob(table, item, selection);
                    job.setSystem(true);
                    job.schedule();
                }
            }
            scrolled.setOrigin(0, 0);
            updateSelectionLabel(selection.getJavaElement());
        }
    }

    private void updateSelectionLabel(final IJavaElement javaElement) {
        selectionLabel.setText(labelProvider.getText(javaElement));
        selectionLabel.setImage(labelProvider.getImage(javaElement));
        selectionLabel.getParent().layout();
    }

    @Override
    public void setFocus() {
        scrolled.forceFocus();
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
