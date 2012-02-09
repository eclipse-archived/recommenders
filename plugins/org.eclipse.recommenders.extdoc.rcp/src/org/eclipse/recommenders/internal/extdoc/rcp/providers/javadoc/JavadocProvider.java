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
 *    Marcel Bruch - changed to own browser-based implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers.javadoc;

import static org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils.resolveSelectionLocationFromJavaElement;
import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class JavadocProvider extends ExtdocProvider {

    private final EventBus workspaceBus;
    private final JavaElementResolver resolver;

    @Inject
    public JavadocProvider(final EventBus workspaceBus, final JavaElementResolver resolver) {
        this.workspaceBus = workspaceBus;
        this.resolver = resolver;
    }

    @JavaSelectionSubscriber
    public Status onCompilationUnitSelection(final ITypeRoot root, final JavaSelectionEvent selection,
            final Composite parent) throws JavaModelException {
        final IType type = root.findPrimaryType();
        if (type != null) {
            render(type, parent);
        }
        return Status.OK;
    }

    @JavaSelectionSubscriber
    public Status onTypeSelection(final IType type, final JavaSelectionEvent selection, final Composite parent)
            throws JavaModelException {
        render(type, parent);
        return Status.OK;
    }

    @JavaSelectionSubscriber
    public Status onMethodSelection(final IMethod method, final JavaSelectionEvent selection, final Composite parent)
            throws JavaModelException {
        render(method, parent);
        return Status.OK;
    }

    @JavaSelectionSubscriber
    public Status onFieldSelection(final IField field, final JavaSelectionEvent selection, final Composite parent)
            throws JavaModelException {
        render(field, parent);
        return Status.OK;
    }

    private Status render(final IMember element, final Composite parent) throws JavaModelException {
        final String html = findJavadoc(element);

        final CountDownLatch latch = new CountDownLatch(1);
        runSyncInUiThread(new Runnable() {
            @Override
            public void run() {
                final Browser browser = new Browser(parent, SWT.NONE);
                final Point maxBrowserSize = findParentWithSizeInformation(parent);

                final GridData browserLayoutData = new GridData(maxBrowserSize.x, 0);
                browser.setLayoutData(browserLayoutData);
                browser.setSize(maxBrowserSize.x, 0);
                browser.setText(html);
                browser.addProgressListener(new ProgressAdapter() {
                    @Override
                    public void completed(final ProgressEvent event) {

                        browser.getDisplay().timerExec(5, new Runnable() {

                            @Override
                            public void run() {
                                final String script = "function getDocHeight() { var D = document; return Math.max( Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),Math.max(D.body.clientHeight, D.documentElement.clientHeight));} return getDocHeight();";
                                Double result = (Double) browser.evaluate(script);
                                if (result == null) {
                                    // terminate re-layout operation if browser
                                    // widget fails to compute its size
                                    result = Double.valueOf(SWT.DEFAULT);
                                }
                                final int height = (int) Math.ceil(result.doubleValue());
                                final int width = maxBrowserSize.x;
                                final Point computeSize = browser.computeSize(width, height);
                                browserLayoutData.widthHint = computeSize.x;
                                browserLayoutData.heightHint = computeSize.y;
                                browser.setSize(computeSize);
                                latch.countDown();
                            }
                        });
                    };
                });
                browser.addLocationListener(JavaElementLinks
                        .createLocationListener(new JavaElementLinks.ILinkHandler() {

                            @Override
                            public void handleDeclarationLink(final IJavaElement target) {
                                try {
                                    JavaUI.openInEditor(target);
                                } catch (final PartInitException e) {
                                    JavaPlugin.log(e);
                                } catch (final JavaModelException e) {
                                    JavaPlugin.log(e);
                                }
                            }

                            @Override
                            public boolean handleExternalLink(final URL url, final Display display) {
                                try {
                                    if (url.getProtocol().equals("file")) {
                                        // sometimes we have /, sometimes we have ///
                                        String path = url.getPath();
                                        path = StringUtils.removeStart(path, "///");
                                        path = StringUtils.removeStart(path, "/");
                                        final String type = "L" + StringUtils.substring(path, 0, -".html".length());
                                        final VmTypeName typeName = VmTypeName.get(type);
                                        final Optional<IType> opt = resolver.toJdtType(typeName);
                                        if (opt.isPresent()) {
                                            workspaceBus.post(new JavaSelectionEvent(opt.get(),
                                                    JavaSelectionLocation.METHOD_DECLARATION));
                                        }

                                    } else {
                                        try {
                                            PlatformUI.getWorkbench().getBrowserSupport()
                                                    .createBrowser("recommenders.javadoc").openURL(url);
                                        } catch (final PartInitException e) {
                                            RecommendersPlugin.logError(e, "Error during javadoc selection %s", url);
                                        }
                                    }
                                } catch (final Exception e) {
                                    RecommendersPlugin.logError(e, "Error during javadoc selection %s", url);
                                }
                                return true;
                            }

                            @Override
                            public void handleInlineJavadocLink(final IJavaElement target) {
                                final JavaSelectionLocation location = resolveSelectionLocationFromJavaElement(target);
                                workspaceBus.post(new JavaSelectionEvent(target, location));
                            }

                            @Override
                            public void handleJavadocViewLink(final IJavaElement target) {
                                handleInlineJavadocLink(target);
                            }

                            @Override
                            public void handleTextSet() {
                            }
                        }));
            }

            private Point findParentWithSizeInformation(Composite parent) {
                while (parent != null) {
                    final Point size = parent.getSize();
                    if (size.x > 0) {
                        return size;
                    }
                    parent = parent.getParent();
                }
                throw throwUnreachable();
            }
        });
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
        }
        return Status.OK;
    }

    private static final String fgStyleSheet = loadStyleSheet();

    private String findJavadoc(final IMember element) throws JavaModelException {
        String html = JavadocContentAccess2.getHTMLContent(element, true);
        if (html == null) {
            html = "no javadoc found";
        }

        final int max = Math.min(100, html.length());
        if (html.substring(0, max).indexOf("<html>") != -1) {
            // there is already a header
            return html;
        }

        final StringBuffer info = new StringBuffer(512 + html.length());
        HTMLPrinter.insertPageProlog(info, 0, fgStyleSheet);
        info.append(html);
        HTMLPrinter.addPageEpilog(info);
        return info.toString();

    }

    private static String loadStyleSheet() {
        final Bundle bundle = Platform.getBundle(JavaPlugin.getPluginId());
        final URL styleSheetURL = bundle.getEntry("/JavadocViewStyleSheet.css"); //$NON-NLS-1$
        if (styleSheetURL == null) {
            return null;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(styleSheetURL.openStream()));
            final StringBuffer buffer = new StringBuffer(1500);
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                buffer.append('\n');
                line = reader.readLine();
            }

            final FontData fontData = JFaceResources.getFontRegistry().getFontData(
                    PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
            return HTMLPrinter.convertTopLevelFont(buffer.toString(), fontData);
        } catch (final IOException ex) {
            JavaPlugin.log(ex);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (final IOException e) {
            }
        }
    }
}