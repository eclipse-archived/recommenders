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
package org.eclipse.recommenders.internal.extdoc.rcp.providers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.extdoc.rcp.AbstractTitledProvider;
import org.eclipse.recommenders.extdoc.rcp.ProviderUiUpdateJob;
import org.eclipse.recommenders.extdoc.rcp.SwtFactory;
import org.eclipse.recommenders.extdoc.rcp.UiUtils;
import org.eclipse.recommenders.extdoc.rcp.feedback.CommunityFeedback;
import org.eclipse.recommenders.extdoc.rcp.feedback.IUserFeedbackServer;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.swt.BrowserSizeWorkaround;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.ElementResolver;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.MockedViewSite;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.VariableResolver;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class JavadocProvider extends AbstractTitledProvider {

    private final IUserFeedbackServer server;
    private final Map<Composite, ExtendedJavadocView> javadocs = new HashMap<Composite, ExtendedJavadocView>();
    private final Map<Composite, Composite> feedbackComposites = new HashMap<Composite, Composite>();

    @Inject
    public JavadocProvider(final IUserFeedbackServer server) {
        this.server = server;
    }

    @Override
    protected Composite createContentComposite(final Composite parent) {
        final Composite composite = SwtFactory.createGridComposite(parent, 1, 0, 8, 0, 0);
        final ExtendedJavadocView javadoc = new ExtendedJavadocView(composite, getWorkbenchWindow());
        javadocs.put(composite, javadoc);
        feedbackComposites.put(composite, SwtFactory.createGridComposite(parent, 2, 0, 0, 0, 0));

        if (javadoc.getControl() instanceof Browser) {
            new BrowserSizeWorkaround((Browser) javadoc.getControl());
        }
        return composite;
    }

    @Override
    public ProviderUiUpdateJob updateSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = getJavaElement(selection.getJavaElement());
        return javaElement == null ? displayContent(selection.getJavaElement(), false) : displayContent(javaElement,
                true);
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    public boolean hideOnTimeout() {
        return true;
    }

    private static IJavaElement getJavaElement(final IJavaElement javaElement) {
        if (javaElement instanceof ILocalVariable) {
            return ElementResolver.toJdtType(VariableResolver.resolveTypeSignature((ILocalVariable) javaElement));
        }
        try {
            IJavaElement element = javaElement;
            while (true) {
                if (element == null) {
                    return null;
                }
                final boolean hasJavadocInSource = element instanceof IMember
                        && ((IMember) element).getJavadocRange() != null;
                if (hasJavadocInSource || element.getAttachedJavadoc(null) != null) {
                    return element;
                }
                if (element instanceof IMethod) {
                    final IJavaElement firstDeclaration = JdtUtils.findOverriddenMethod((IMethod) element).orNull();
                    if (element.equals(firstDeclaration)) {
                        return null;
                    }
                    element = firstDeclaration;
                } else {
                    return null;
                }
            }
        } catch (final JavaModelException e) {
            return null;
        }
    }

    private ProviderUiUpdateJob displayContent(final IJavaElement javaElement, final boolean communityFeedback) {
        if (javaElement == null) {
            return null;
        }
        final IName name = ElementResolver.resolveName(javaElement);
        final CommunityFeedback features = communityFeedback ? CommunityFeedback.create(name, null, this, server)
                : null;
        return new ProviderUiUpdateJob() {
            @Override
            public void run(final Composite composite) {
                javadocs.get(composite).setInput(javaElement);
                final Composite feedbackComposite = feedbackComposites.get(composite);
                UiUtils.disposeChildren(feedbackComposite);
                if (features != null) {
                    features.loadCommentsComposite(feedbackComposite);
                    features.loadStarsRatingComposite(feedbackComposite);
                }
            }
        };
    }

    /**
     * Extension to gain access to getControl().
     */
    private static final class ExtendedJavadocView extends JavadocView {

        ExtendedJavadocView(final Composite parent, final IWorkbenchWindow window) {
            setSite(new MockedViewSite(window));
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
