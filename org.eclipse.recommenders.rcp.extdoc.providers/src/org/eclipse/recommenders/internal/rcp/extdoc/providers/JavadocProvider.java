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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.VariableResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("restriction")
public final class JavadocProvider extends AbstractProviderComposite {

    private ExtendedJavadocView javadoc;
    private BrowserSizeWorkaround browserSizeWorkaround;

    @Override
    protected Control createContentControl(final Composite parent) {
        javadoc = new ExtendedJavadocView(parent, getPartSite());

        if (javadoc.getControl() instanceof Browser) {
            browserSizeWorkaround = new BrowserSizeWorkaround((Browser) javadoc.getControl());
        }
        return javadoc.getControl();
    }

    @Override
    public boolean selectionChanged(final IJavaElementSelection selection) {
        try {
            final IJavaElement javaElement = getJavaElement(selection.getJavaElement());
            if (javaElement == null) {
                return false;
            }
            selection.getJavaElement().getAttachedJavadoc(null);
            javadoc.setInput(javaElement);
            // TODO: Do we need this?
            // browserSizeWorkaround.switchToMinimumSize();
            return true;
        } catch (final JavaModelException e) {
            return false;
        }
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    private IJavaElement getJavaElement(final IJavaElement javaElement) {
        if (javaElement instanceof ILocalVariable) {
            return VariableResolver.resolveTypeSignature((ILocalVariable) javaElement);
        }
        return javaElement;
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

        @Override
        public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
            // Ignore, we set the selection.
        }

        @Override
        protected Object computeInput(final IWorkbenchPart part, final ISelection selection, final IJavaElement input,
                final IProgressMonitor monitor) {
            final Object defaultInput = super.computeInput(part, selection, input, monitor);
            if (defaultInput instanceof String) {
                final String javaDocHtml = (String) defaultInput;
                final String htmlBeforeTitle = StringUtils.substringBefore(javaDocHtml, "<h5>");
                final String htmlAfterTitle = StringUtils.substringAfter(javaDocHtml, "</h5>");
                return htmlBeforeTitle + htmlAfterTitle;
            }
            return defaultInput;
        }

    }

}
