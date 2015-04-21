/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import static org.eclipse.recommenders.internal.apidocs.rcp.ApidocsViewUtils.*;
import static org.eclipse.recommenders.utils.Throws.throwUnsupportedOperation;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class ProviderArea {

    private final ApidocProvider provider;

    private Composite container;

    private Composite title;
    private Composite status;
    private Composite content;

    private final GridLayoutFactory layoutFactory = GridLayoutFactory.fillDefaults().spacing(0, 0);
    private final GridDataFactory layoutDataFactory = GridDataFactory.fillDefaults().grab(true, false);

    public ProviderArea(final ApidocProvider provider) {
        this.provider = provider;
    }

    public void createControl(final Composite parent) {
        container = createComposite(parent);

        Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(layoutDataFactory.indent(0, 5).create());

        createTitleArea();
        status = createComposite(container);
        content = createComposite(container);

        hide();
    }

    private void createTitleArea() {
        title = createComposite(container);

        final String providerName = provider.getDescription().getName();
        final Image providerImage = provider.getDescription().getImage();

        final CLabel l = new CLabel(title, SWT.NONE);
        l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        setInfoForegroundColor(l);
        setInfoBackgroundColor(l);
        l.setText(providerName);
        l.setImage(providerImage);
    }

    private Composite createComposite(final Composite parent) {
        final Composite c = new Composite(parent, SWT.NONE);
        setInfoBackgroundColor(c);
        c.setLayout(layoutFactory.create());
        c.setLayoutData(layoutDataFactory.create());
        return c;
    }

    public Composite getContentArea() {
        return content;
    }

    public void showContent() {
        setVisible(title, true);
        setVisible(status, false);
        setVisible(content, true);
        setVisible(container, true);
        layout();
    }

    public void showStatus() {
        setVisible(title, true);
        setVisible(status, true);
        setVisible(content, false);
        setVisible(container, true);
        layout();
    }

    public void hide() {
        setVisible(title, false);
        setVisible(status, false);
        setVisible(content, false);
        setVisible(container, false);
        layout();
    }

    /**
     * relayout is needed afterwards
     */
    private static void setVisible(final Composite d, final boolean isVisible) {
        d.setVisible(isVisible);
        final Object layoutData = d.getLayoutData();
        if (layoutData instanceof GridData) {
            final GridData gridData = (GridData) layoutData;
            gridData.exclude = !isVisible;
            // if (isVisible) {
            // gridData.heightHint = -1;
            // } else {
            // gridData.heightHint = 0;
            // }
            // d.setLayoutData(gridData);
        } else {
            throwUnsupportedOperation("layout of provided composite not supported"); //$NON-NLS-1$
        }
    }

    public void layout() {
        status.layout();
        content.layout();
        container.layout();
    }

    public Point getLocation() {
        return container.getLocation();
    }

    public void moveAbove(final ProviderArea area) {
        container.moveAbove(area.container);
    }

    public void moveBelow(final ProviderArea area) {
        container.moveBelow(area.container);
    }

    public void cleanup() {
        disposeChildren(status);
        disposeChildren(content);
    }

    private static void disposeChildren(final Composite parent) {
        for (final Control child : parent.getChildren()) {
            child.dispose();
        }
    }

    public void setStatus(final String statusMessage) {
        disposeChildren(status);
        final Link link = new Link(status, SWT.NONE);
        link.setText(statusMessage);
        link.setBackground(link.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        layout();
    }

    public void setStatusWithCallback(final String statusMessage, final SelectionListener listener) {
        disposeChildren(status);
        final Link link = new Link(status, SWT.NONE);
        link.setText(statusMessage);
        link.setBackground(link.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        link.addSelectionListener(listener);
        layout();
    }
}
