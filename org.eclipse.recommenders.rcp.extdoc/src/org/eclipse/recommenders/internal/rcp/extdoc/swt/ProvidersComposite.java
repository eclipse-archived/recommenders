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
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;

final class ProvidersComposite extends Composite {

    private final ScrolledComposite scrolled;
    private final List<Composite> providers = new LinkedList<Composite>();

    ProvidersComposite(final Composite parent, final boolean setGridData) {
        super(createScrolledComposite(parent), SWT.NONE);
        scrolled = (ScrolledComposite) getParent();
        if (setGridData) {
            scrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        }
        setLayout();
        setBackgroundColor(scrolled.getShell().getDisplay());
        scrolled.setContent(this);
    }

    private static ScrolledComposite createScrolledComposite(final Composite parent) {
        final ScrolledComposite composite = new ScrolledComposite(parent, SWT.V_SCROLL);
        composite.setExpandVertical(true);
        composite.setExpandHorizontal(true);
        composite.getVerticalBar().setIncrement(20);
        return composite;
    }

    private void setLayout() {
        final GridLayout grid = new GridLayout(1, false);
        grid.verticalSpacing = 4;
        grid.marginWidth = 0;
        grid.marginHeight = 0;
        grid.horizontalSpacing = 0;
        setLayout(grid);
    }

    private void setBackgroundColor(final Display display) {
        final ColorRegistry registry = JFaceResources.getColorRegistry();
        final RGB backgroundColor = registry.getRGB("org.eclipse.jdt.ui.JavadocView.backgroundColor");
        if (backgroundColor == null) {
            setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        } else {
            setBackground(new Color(display, backgroundColor));
        }
        setBackgroundMode(SWT.INHERIT_FORCE);
    }

    Composite addProvider(final IProvider provider, final IViewSite viewSite) {
        final Composite control = provider.createComposite(this, viewSite);
        control.setData(provider);
        providers.add(control);
        return control;
    }

    List<Composite> getProviders() {
        return providers;
    }

    @Override
    public void layout(final boolean changed, final boolean all) {
        super.layout(changed, all);
        scrolled.setMinHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y + 15);
    }

    @Override
    public boolean setFocus() {
        return scrolled.forceFocus();
    }

    public void scrollToTop() {
        scrolled.setOrigin(0, 0);
    }

    public void scrollToProvider(final Composite providerComposite) {
        scrolled.setOrigin(providerComposite.getLocation());
    }

}
