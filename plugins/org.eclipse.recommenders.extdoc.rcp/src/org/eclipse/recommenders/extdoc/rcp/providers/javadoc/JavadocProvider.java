/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Sebastian Proksch - integrated into new eventbus system
 */
package org.eclipse.recommenders.extdoc.rcp.providers.javadoc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.ProviderDescription;
import org.eclipse.recommenders.extdoc.rcp.scheduling.SubscriptionManager.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.ui.ExtdocIconLoader;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Optional;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class JavadocProvider extends Provider {

    private ProviderDescription description;

    private IWorkbenchWindow activeWorkbenchWindow;
    private ExtendedJavadocView javadoc;

    @Inject
    public JavadocProvider(ExtdocIconLoader iconLoader) {
        activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        description = new ProviderDescription("JavadocProvider", iconLoader.getImage("provider.javadoc.gif"));
    }

    @Override
    public ProviderDescription getDescription() {
        return description;
    }

    @JavaSelectionListener
    public void displayProposalsForType(IJavaElement element, JavaSelectionEvent selection, final Composite parent)
            throws InterruptedException {

        final IJavaElement javaElement = findDocumentedJavaElement(element);
        runSyncInUiThread(new Runnable() {
            @Override
            public void run() {
                javadoc = new ExtendedJavadocView(parent, activeWorkbenchWindow);
                if (javadoc.getControl() instanceof Browser) {
                    new BrowserSizeWorkaround((Browser) javadoc.getControl());
                }
                javadoc.setInput(javaElement);
            }
        });
        Thread.sleep(BrowserSizeWorkaround.MS_UNTIL_RESCALE + 50);
    }

    private static IJavaElement findDocumentedJavaElement(final IJavaElement element) {
        try {
            if (element == null) {
                return null;
            }

            if (element instanceof ILocalVariable) {
                ITypeName typeName = VariableResolver.resolveTypeSignature((ILocalVariable) element);
                return ElementResolver.toJdtType(typeName);
            }

            if (hasJavadocInSource(element) || canGetAttachedJavadoc(element)) {
                return element;
            }

            if (element instanceof IMethod) {
                final Optional<IMethod> firstDeclaration = JdtUtils.findOverriddenMethod((IMethod) element);
                if (firstDeclaration.isPresent()) {
                    if (!element.equals(firstDeclaration)) {
                        return findDocumentedJavaElement(firstDeclaration.get());
                    }
                }
            }
        } catch (final JavaModelException e) {
            // blubb
        }

        return element;
    }

    private static boolean canGetAttachedJavadoc(IJavaElement element) throws JavaModelException {
        return element.getAttachedJavadoc(null) != null;
    }

    private static boolean hasJavadocInSource(IJavaElement element) throws JavaModelException {
        // try {
        if (element instanceof IMember) {
            IMember member = (IMember) element;
            return member.getJavadocRange() != null;
        } else {
            return false;
        }
        // } catch (RuntimeException e) {
        // return false;
        // }
    }

    private static final class ExtendedJavadocView extends JavadocView {

        public ExtendedJavadocView(final Composite parent, final IWorkbenchWindow window) {
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
                String beforeTitle = StringUtils.substringBefore(javaDocHtml, "<h5>");
                String afterTitle = StringUtils.substringAfter(javaDocHtml, "</h5>");
                if (afterTitle.startsWith("<br>")) {
                    afterTitle = afterTitle.substring(4);
                }
                String replacedHtml = removeMarginOfBodyElement(beforeTitle) + afterTitle;
                // System.out.println(replacedHtml);

                replacedHtml = isHtmlBodyEmpty(replacedHtml);
                return replacedHtml;
            }
            return defaultInput;
        }

        private String removeMarginOfBodyElement(String markUp) {

            markUp = markUp.replaceAll("\n", " ");

            Pattern pattern = Pattern.compile(".*(body.*\\{.*\\}).*");
            Matcher matcher = pattern.matcher(markUp);

            if (matcher.matches()) {
                String toReplace = matcher.group(1);
                String replacement = "body { overflow: auto; margin: 0; } p { margin: 3px 0; }";
                markUp = markUp.replace(toReplace, replacement);
            }
            return markUp;
        }

        private String isHtmlBodyEmpty(String markUp) {
            Pattern pattern = Pattern.compile(".*((<body.*?>)(.*?)<\\/body>).*");
            Matcher matcher = pattern.matcher(markUp);
            if (matcher.matches()) {
                String content = matcher.group(3);
                // System.out.println("matched: " + content);
                if (content.equals("")) {
                    content = "<em>Note: No javadoc available</em>";
                    markUp = markUp.replace(matcher.group(1), matcher.group(2) + content + "</body>");
                }
            }

            return markUp;
        }
    }
}