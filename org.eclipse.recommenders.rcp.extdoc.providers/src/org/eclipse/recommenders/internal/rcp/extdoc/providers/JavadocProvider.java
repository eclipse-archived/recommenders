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
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

@SuppressWarnings("restriction")
public final class JavadocProvider implements IProvider {

    private ExtendedJavadocView javadoc;

    @Override
    public Control createControl(final Composite parent, final IWorkbenchPartSite partSite) {
        javadoc = new ExtendedJavadocView(parent, partSite);
        return javadoc.getControl();
    }

    @Override
    public void selectionChanged(final IJavaElementSelection context) {
        if (context.getJavaElement() != null) {
            javadoc.setInput(context.getJavaElement());
        }
    }

    @Override
    public void redraw() {
        // TODO Auto-generated method stub
    }

    /**
     * Extension to gain access to getControl().
     */
    private static class ExtendedJavadocView extends JavadocView {

        public ExtendedJavadocView(final Composite parent, final IWorkbenchPartSite partSite) {
            setSite(partSite);
            createPartControl(parent);
        }

        @Override
        protected Control getControl() {
            return super.getControl();
        }

    }
}
