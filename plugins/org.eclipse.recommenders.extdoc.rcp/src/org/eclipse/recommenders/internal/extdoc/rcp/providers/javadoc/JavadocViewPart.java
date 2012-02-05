/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers.javadoc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.infoviews.JavadocView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocModule.Extdoc;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.google.common.eventbus.EventBus;

/**
 * This class simply reuses to original Eclipse javadoc view to display Javadocs attached to {@link IJavaElement}s. At
 * some time later, we may have the need to create a beautiful solution. For this suffices.
 */
@SuppressWarnings("restriction")
final class JavadocViewPart extends JavadocView {

    private final IJavaElement selection;
    private final EventBus workspaceBus;

    public JavadocViewPart(final Composite parent, final IWorkbenchWindow window, final IJavaElement selection,
            @Extdoc final EventBus extdocBus) {
        this.selection = selection;
        this.workspaceBus = extdocBus;
        setSite(new ViewSiteMock(window.getShell()));
        createPartControl(parent);
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        // we trigger them ourselves
    }

    @Override
    protected Control getControl() {
        return super.getControl();
    }

    @Override
    protected Object computeInput(final IWorkbenchPart part, final ISelection selection, final IJavaElement input,
            final IProgressMonitor monitor) {
        if (isNewInternalJavaElementSelection(input)) {
            fireNewJavaSelectionEvent(input);
            return null;
        } else {
            return useSuperImplementationButStripHeaders(part, selection, input, monitor);
        }
    }

    /**
     * Links in the javadoc viewer are resolved to java elements on click - if possible. If so, we want to trigger a new
     * workspace-wide(!) selection event.
     */
    private boolean isNewInternalJavaElementSelection(final IJavaElement input) {
        return input != this.selection && input != null;
    }

    private void fireNewJavaSelectionEvent(final IJavaElement input) {
        final JavaSelectionLocation location = JavaSelectionUtils.resolveSelectionLocationFromJavaElement(input);
        final JavaSelectionEvent event = new JavaSelectionEvent(input, location);
        workspaceBus.post(event);
    }

    private Object useSuperImplementationButStripHeaders(final IWorkbenchPart part, final ISelection selection,
            final IJavaElement input, final IProgressMonitor monitor) {
        final Object defaultInput = super.computeInput(part, selection, input, monitor);
        if (defaultInput instanceof String) {
            final String javaDocHtml = (String) defaultInput;
            final String beforeTitle = StringUtils.substringBefore(javaDocHtml, "<h5>");
            String afterTitle = StringUtils.substringAfter(javaDocHtml, "</h5>");
            if (afterTitle.startsWith("<br>")) {
                afterTitle = afterTitle.substring(4);
            }
            String replacedHtml = removeMarginOfBodyElement(beforeTitle) + afterTitle;
            replacedHtml = isHtmlBodyEmpty(replacedHtml);
            return replacedHtml;
        }
        return defaultInput;
    }

    private String removeMarginOfBodyElement(String markUp) {

        markUp = markUp.replaceAll("\n", " ");

        final Pattern pattern = Pattern.compile(".*(body.*\\{.*\\}).*");
        final Matcher matcher = pattern.matcher(markUp);

        if (matcher.matches()) {
            final String toReplace = matcher.group(1);
            final String replacement = "body { overflow: auto; margin: 0 0 0 3px; } p { margin: 3px 0; }";
            markUp = markUp.replace(toReplace, replacement);
        }
        return markUp;
    }

    private String isHtmlBodyEmpty(String markUp) {
        final Pattern pattern = Pattern.compile(".*((<body.*?>)(.*?)<\\/body>).*");
        final Matcher matcher = pattern.matcher(markUp);
        if (matcher.matches()) {
            String content = matcher.group(3);
            if (content.equals("")) {
                content = "<em>Note: No javadoc available</em>";
                markUp = markUp.replace(matcher.group(1), matcher.group(2) + content + "</body>");
            }
        }

        return markUp;
    }
}