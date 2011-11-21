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
package org.eclipse.recommenders.extdoc.rcp.selection2;

import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionUtils.resolveJavaElementFromEditor;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionUtils.resolveJavaElementFromViewer;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionUtils.resolveSelectionLocationFromEditor;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionUtils.resolveSelectionLocationFromViewer;
import static org.eclipse.recommenders.utils.Checks.cast;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Optional;

/**
 * Controls which events get fired over the event bus. It internally keeps track of the last selection to prevent the
 * system to send minor selection event updates as frequently happens during typing.
 */
@SuppressWarnings("restriction")
public class JavaSelectionListener implements ISelectionListener {

    private final JavaSelectionDispatcher dispatcher;
    private JavaSelection lastEvent = new JavaSelection(null, null);

    @Inject
    public JavaSelectionListener(final JavaSelectionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection s = (IStructuredSelection) selection;
            final Optional<IJavaElement> element = resolveJavaElementFromViewer(s);
            if (!element.isPresent()) {
                return;
            }
            final JavaSelectionLocation location = resolveSelectionLocationFromViewer(element.get());
            fireEventIfNew(element, location);

        } else if (selection instanceof ITextSelection && part instanceof JavaEditor) {
            final JavaEditor editor = cast(part);
            final ITextSelection textSelection = cast(selection);
            final Optional<IJavaElement> element = resolveJavaElementFromEditor(editor, textSelection);
            if (!element.isPresent()) {
                return;
            }
            final JavaSelectionLocation location = resolveSelectionLocationFromEditor(editor, textSelection);
            fireEventIfNew(element, location);
        }
    }

    private void fireEventIfNew(final Optional<IJavaElement> element, final JavaSelectionLocation location) {

        final JavaSelection event = new JavaSelection(element.get(), location);

        if (!lastEvent.equals(event)) {
            lastEvent = event;
            dispatcher.fire(event);
        }
    }
}
