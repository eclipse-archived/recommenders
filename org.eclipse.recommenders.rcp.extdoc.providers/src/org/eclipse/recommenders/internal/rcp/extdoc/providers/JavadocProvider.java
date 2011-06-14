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

import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.rcp.extdoc.AbstractProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

@SuppressWarnings("restriction")
public final class JavadocProvider extends AbstractProvider {

    private ExtendedJavadocView javadoc;

    @Override
    public Control createControl(final Composite parent, final IWorkbenchPartSite partSite) {
        javadoc = new ExtendedJavadocView(parent, partSite);
        javadoc.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        javadoc.getControl().setSize(-1, 120);
        return javadoc.getControl();
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    public boolean selectionChanged(final IJavaElementSelection context) {
        if (context.getJavaElement() != null) {
            javadoc.setInput(context.getJavaElement());
        }
        return context.getJavaElement() != null;
    }

    @Override
    public void redraw() {
        throw new IllegalAccessError("No need to redraw a JavadocView.");
    }

    @Override
    public Shell getShell() {
        throw new IllegalAccessError("No need to access the shell.");
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
