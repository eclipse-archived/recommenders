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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.rcp.extdoc.AbstractProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

@SuppressWarnings("restriction")
public final class JavadocProvider extends AbstractProvider implements ProgressListener {

    private ExtendedJavadocView javadoc;

    @Override
    public Control createControl(final Composite parent, final IWorkbenchPartSite partSite) {
        javadoc = new ExtendedJavadocView(parent, partSite);
        setBrowserSizeLayoutDataAndTriggerLayout(20);
        final Browser browser = (Browser) javadoc.getControl();
        browser.addProgressListener(this);
        browser.setJavascriptEnabled(true);
        return browser;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    public boolean selectionChanged(final IJavaElementSelection context) {
        try {
            context.getJavaElement().getAttachedJavadoc(null);
            javadoc.setInput(context.getJavaElement());
            setBrowserSizeLayoutDataAndTriggerLayout(20);
            return true;
        } catch (final JavaModelException e) {
            return false;
        }
    }

    @Override
    public Shell getShell() {
        throw new IllegalAccessError("No need to access the shell.");
    }

    @Override
    public void changed(final ProgressEvent event) {
    }

    @Override
    public void completed(final ProgressEvent event) {
        Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
                final Browser browser = (Browser) javadoc.getControl();
                final Object result = browser
                        .evaluate("function getDocHeight() { var D = document; return Math.max( Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),Math.max(D.body.clientHeight, D.documentElement.clientHeight));} return getDocHeight();");
                final int height = (int) Math.ceil((Double) result);
                setBrowserSizeLayoutDataAndTriggerLayout(height);
            }
        });
    }

    private void setBrowserSizeLayoutDataAndTriggerLayout(final int height) {
        final Control control = javadoc.getControl();
        GridData data = (GridData) control.getLayoutData();
        if (data == null) {
            data = GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, height)
                    .minSize(SWT.DEFAULT, height).create();
            control.setLayoutData(data);
        }
        data.heightHint = height;
        data.minimumHeight = height;
        control.getParent().layout();
    }

    /**
     * Extension to gain access to getControl().
     */
    private static final class ExtendedJavadocView extends JavadocView {

        private ExtendedJavadocView(final Composite parent, final IWorkbenchPartSite partSite) {
            setSite(partSite);
            createPartControl(parent);
        }

        @Override
        protected Control getControl() {
            return super.getControl();
        }

    }

}
