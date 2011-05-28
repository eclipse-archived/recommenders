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
package org.eclipse.recommenders.rcp.extdoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.commons.selection.ExtendedSelectionContext;
import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.internal.rcp.extdoc.listener.BrowserLinkListener;
import org.eclipse.recommenders.rcp.extdoc.listener.IBrowserListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

@SuppressWarnings("restriction")
public abstract class AbstractBrowserProvider implements IProvider {

    private static final long LABEL_FLAGS = JavaElementLabels.ALL_FULLY_QUALIFIED | JavaElementLabels.M_PRE_RETURNTYPE
            | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES
            | JavaElementLabels.M_EXCEPTIONS | JavaElementLabels.F_PRE_TYPE_SIGNATURE
            | JavaElementLabels.T_TYPE_PARAMETERS;
    private static String styleSheet;
    private static final Object LOCK = new Object();

    private Browser browser;
    private final BrowserLinkListener linkListener = new BrowserLinkListener();

    private ExtendedSelectionContext lastContext;

    @Override
    public final Control createControl(final Composite parent, final IWorkbenchPartSite partSite) {
        browser = new Browser(parent, SWT.NONE);
        browser.setJavascriptEnabled(false);
        browser.addLocationListener(linkListener);
        return browser;
    }

    @Override
    public final void selectionChanged(final ExtendedSelectionContext context) {
        lastContext = context;
        if (context.getJavaElement() != null) {
            final String txt = getHtmlContent(context);
            final StringBuffer buffer = new StringBuffer();
            HTMLPrinter.addSmallHeader(buffer, getInfoText(context.getJavaElement()));
            HTMLPrinter.addParagraph(buffer, txt);
            HTMLPrinter.insertPageProlog(buffer, 0, getStyleSheet());
            HTMLPrinter.addPageEpilog(buffer);
            browser.setText(buffer.toString());
        }
    }

    protected final void reload() {
        selectionChanged(lastContext);
    }

    protected final Shell getShell() {
        return browser.getShell();
    }

    protected final String addListenerAndGetHtml(final IBrowserListener listener) {
        final int hash = linkListener.addListener(listener);
        return listener.getHtml("#" + hash);
    }

    protected abstract String getHtmlContent(final ExtendedSelectionContext context);

    private String getInfoText(final IJavaElement element) {
        String imageName = null;
        final URL imageUrl = JavaPlugin.getDefault().getImagesOnFSRegistry().getImageURL(element);
        if (imageUrl != null) {
            imageName = imageUrl.toExternalForm();
        }

        final String label = JavaElementLinks.getElementLabel(element, LABEL_FLAGS);
        final StringBuffer buf = new StringBuffer();
        JavadocHover.addImageAndLabel(buf, imageName, 16, 16, label.toString(), 20, 2);
        return buf.toString();
    }

    private static String getStyleSheet() {
        synchronized (LOCK) {
            if (styleSheet == null) {
                try {
                    styleSheet = loadStyleSheet();
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return styleSheet;
    }

    private static String loadStyleSheet() throws IOException {
        final URL styleSheetURL = ExtDocPlugin.getBundleEntry("/stylesheet.css");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(styleSheetURL.openStream()));
            final StringBuffer buffer = new StringBuffer(1500);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                buffer.append(line);
                buffer.append('\n');
            }
            final FontData fontData = JFaceResources.getFontRegistry().getFontData(
                    PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
            return HTMLPrinter.convertTopLevelFont(buffer.toString(), fontData);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
