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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.BrowserSizeWorkaround;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.VariableResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.CommunityFeatures;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("restriction")
public final class JavadocProvider extends AbstractProviderComposite {

    private Composite composite;
    private ExtendedJavadocView javadoc;
    private final GenericServer server;
    private Composite feedbackComposite;

    @Inject
    public JavadocProvider(final GenericServer server) {
        this.server = server;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 8, 0, 0);
        javadoc = new ExtendedJavadocView(composite, getViewSite());
        feedbackComposite = SwtFactory.createGridComposite(parent, 2, 0, 0, 0, 0);

        if (javadoc.getControl() instanceof Browser) {
            new BrowserSizeWorkaround((Browser) javadoc.getControl());
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
            displayComments(selection.getJavaElement());
            return true;
        } catch (final JavaModelException e) {
            return false;
        }
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    private static IJavaElement getJavaElement(final IJavaElement javaElement) {
        if (javaElement instanceof ILocalVariable) {
            return ElementResolver.toJdtType(VariableResolver.resolveTypeSignature((ILocalVariable) javaElement));
        }
        return javaElement;
    }

    private void displayComments(final IJavaElement javaElement) {
        final CommunityFeatures features = CommunityFeatures.create(ElementResolver.resolveName(javaElement), null,
                this, server);
        new UIJob("Updating JavaDoc Provider") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!composite.isDisposed()) {
                    disposeChildren(feedbackComposite);
                    if (features != null) {
                        features.loadCommentsComposite(feedbackComposite);
                        features.loadStarsRatingComposite(feedbackComposite);
                    }
                    feedbackComposite.layout(true);
                    composite.getParent().getParent().layout(true);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    /**
     * Extension to gain access to getControl().
     */
    private static final class ExtendedJavadocView extends JavadocView {

        ExtendedJavadocView(final Composite parent, final IViewSite viewSite) {
            setSite(viewSite);
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
