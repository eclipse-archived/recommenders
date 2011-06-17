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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

@SuppressWarnings("restriction")
public final class JavadocProvider extends AbstractProviderComposite {

    private ExtendedJavadocView javadoc;
    private BrowserSizeWorkaround browserSizeWorkaround;
    private JavaElementLabelProvider labelProvider;

    @Override
    protected Control createContentControl(final Composite parent) {
        javadoc = new ExtendedJavadocView(parent, getSite());

        if (javadoc.getControl() instanceof Browser) {
            browserSizeWorkaround = new BrowserSizeWorkaround((Browser) javadoc.getControl());
        }
        labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_QUALIFIED
                | JavaElementLabelProvider.SHOW_OVERLAY_ICONS | JavaElementLabelProvider.SHOW_RETURN_TYPE
                | JavaElementLabelProvider.SHOW_PARAMETERS);
        return javadoc.getControl();
    }

    @Override
    protected boolean updateContent(final IJavaElementSelection context) {
        try {
            context.getJavaElement().getAttachedJavadoc(null);
            javadoc.setInput(context.getJavaElement());
            browserSizeWorkaround.switchToMinimumSize();
            setTitleByJavaElement(context.getJavaElement());
            return true;
        } catch (final JavaModelException e) {
            return false;
        }
    }

    private void setTitleByJavaElement(final IJavaElement javaElement) {
        setTitle(labelProvider.getText(javaElement));
        setTitleIcon(labelProvider.getImage(javaElement));
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
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
        protected Object computeInput(final IWorkbenchPart part, final ISelection selection, final IJavaElement input,
                final IProgressMonitor monitor) {
            final Object defaultInput = super.computeInput(part, selection, input, monitor);
            if (defaultInput instanceof String) {
                final String javaDocHtml = (String) defaultInput;
                final String htmlBeforeTitle = StringUtils.substringBefore(javaDocHtml, "<h5>");
                final String htmlAfterTitle = StringUtils.substringAfter(javaDocHtml, "</h5><br>");
                return htmlBeforeTitle + htmlAfterTitle;
            }

            return defaultInput;
        }

    }

}
