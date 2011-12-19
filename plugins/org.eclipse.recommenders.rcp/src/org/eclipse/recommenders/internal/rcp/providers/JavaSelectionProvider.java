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
package org.eclipse.recommenders.internal.rcp.providers;

import static org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils.resolveJavaElementFromEditor;
import static org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils.resolveSelectionLocationFromAstNode;
import static org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils.resolveSelectionLocationFromJavaElement;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findAstNodeFromEditorSelection;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation;
import org.eclipse.recommenders.utils.rcp.RCPUtils;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

/**
 * Controls which events get fired over the event bus. It internally keeps track of the last selection to prevent the
 * system to send minor selection event updates as frequently happens during typing.
 */
@SuppressWarnings("restriction")
public class JavaSelectionProvider implements ISelectionListener {

    private final EventBus bus;
    private JavaSelectionEvent lastEvent = new JavaSelectionEvent(null, null);

    @Inject
    public JavaSelectionProvider(final EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            handleSelectionFromViewer(selection);
        } else if (selection instanceof ITextSelection && part instanceof JavaEditor) {
            handleSelectionInEditor(part, selection);
        }
    }

    private void handleSelectionFromViewer(final ISelection selection) {
        final Optional<IJavaElement> element = RCPUtils.first(selection);
        if (element.isPresent()) {
            final JavaSelectionLocation location = resolveSelectionLocationFromJavaElement(element.get());
            fireEventIfNew(element.get(), location, null);
        }
    }

    private void fireEventIfNew(final IJavaElement element, final JavaSelectionLocation location,
            final ASTNode selectedNode) {
        final JavaSelectionEvent event = new JavaSelectionEvent(element, location, selectedNode);
        if (!lastEvent.equals(event)) {
            lastEvent = event;
            bus.post(event);
        }
    }

    private void handleSelectionInEditor(final IWorkbenchPart part, final ISelection selection) {
        final JavaEditor editor = cast(part);
        final ITextSelection textSelection = cast(selection);
        final Optional<IJavaElement> element = resolveJavaElementFromEditor(editor, textSelection);
        if (element.isPresent()) {
            final ASTNode node = findAstNodeFromEditorSelection(editor, textSelection).orNull();
            final JavaSelectionLocation location = resolveSelectionLocationFromAstNode(node);
            fireEventIfNew(element.get(), location, node);
        }
    }
}
