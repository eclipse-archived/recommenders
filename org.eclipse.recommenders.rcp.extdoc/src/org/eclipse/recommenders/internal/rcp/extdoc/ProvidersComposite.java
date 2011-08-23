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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.internal.rcp.extdoc.view.ExtDocView;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Displays all provider content in a vertical layout.
 */
public final class ProvidersComposite extends Composite {

    private ScrolledComposite scrolled;
    private Composite contentComposite;
    private final List<Composite> providers = new LinkedList<Composite>();
    private final IWorkbenchWindow workbenchWindow;

    private JavaElementLabelProvider labelProvider;
    private CLabel selectionLabel;

    /**
     * @param parent
     *            The SWT composite which will host the providers composite.
     * @param workbenchWindow
     *            Some providers may require access to the current workbench
     *            window.
     */
    public ProvidersComposite(final Composite parent, final IWorkbenchWindow workbenchWindow) {
        super(parent, SWT.NONE);
        setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
        createSelectionLabel();
        createScrolledComposite();
        setBackgroundColor(scrolled.getShell().getDisplay());
        this.workbenchWindow = Checks.ensureIsNotNull(workbenchWindow);
    }

    private void createSelectionLabel() {
        labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_QUALIFIED
                | JavaElementLabelProvider.SHOW_OVERLAY_ICONS | JavaElementLabelProvider.SHOW_RETURN_TYPE
                | JavaElementLabelProvider.SHOW_PARAMETERS);
        selectionLabel = new CLabel(this, SWT.NONE);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gridData.heightHint = ExtDocView.HEAD_LABEL_HEIGHT;
        selectionLabel.setLayoutData(gridData);
        selectionLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
    }

    private void createScrolledComposite() {
        scrolled = new ScrolledComposite(this, SWT.V_SCROLL);
        scrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolled.setExpandVertical(true);
        scrolled.setExpandHorizontal(true);
        scrolled.getVerticalBar().setIncrement(20);

        contentComposite = new Composite(scrolled, SWT.NONE);
        contentComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 2).create());
        scrolled.setContent(contentComposite);
    }

    private void setBackgroundColor(final Display display) {
        final ColorRegistry registry = JFaceResources.getColorRegistry();
        final RGB backgroundColor = registry.getRGB("org.eclipse.jdt.ui.JavadocView.backgroundColor");
        if (backgroundColor == null) {
            contentComposite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        } else {
            contentComposite.setBackground(new Color(display, backgroundColor));
        }
        contentComposite.setBackgroundMode(SWT.INHERIT_FORCE);
    }

    /**
     * @param provider
     *            The provider which shall be registered with the composite.
     * @return The composite in which the provider will fill his content.
     */
    public Composite addProvider(final IProvider provider) {
        final Composite control = provider.createComposite(contentComposite, workbenchWindow);
        control.setData(provider);
        providers.add(control);
        return control;
    }

    List<Composite> getProviders() {
        return providers;
    }

    public void updateSelectionLabel(final IJavaElement javaElement) {
        selectionLabel.setText(labelProvider.getText(javaElement));
        selectionLabel.setImage(labelProvider.getImage(javaElement));
        selectionLabel.getParent().layout();
    }

    @Override
    public void layout(final boolean changed, final boolean all) {
        contentComposite.layout(changed, all);
        scrolled.setMinHeight(contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y + 15);
    }

    @Override
    public boolean setFocus() {
        return scrolled.forceFocus();
    }

    /**
     * Resets scroll bar position to the top of the composite.
     */
    public void scrollToTop() {
        scrolled.setOrigin(0, 0);
    }

    void scrollToProvider(final Composite providerComposite) {
        scrolled.setOrigin(providerComposite.getLocation());
    }

}
