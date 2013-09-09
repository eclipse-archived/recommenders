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
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.internal.rcp.JavaElementSelections.*;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findAstNodeFromEditorSelection;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent.JavaElementSelectionLocation;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Controls which events get fired over the event bus. It internally keeps track of the last selection to prevent the
 * system to send minor selection event updates as frequently happens during typing.
 */
@SuppressWarnings("restriction")
public class JavaElementSelectionService implements ISelectionListener {

    ScheduledThreadPoolExecutor d = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat(
            "Recommenders-Timeout-Manager").build()); //$NON-NLS-1$
    private final EventBus bus;
    private JavaElementSelectionEvent lastEvent = new JavaElementSelectionEvent(null, null);

    @Inject
    public JavaElementSelectionService(final EventBus bus) {
        this.bus = bus;
        bus.register(this);
    }

    /**
     * other parties may send other selection events. we should update our internal state based on this information.
     */
    @Subscribe
    public void onExternalJavaSelectionChange(final JavaElementSelectionEvent newSelectionEvent) {
        lastEvent = newSelectionEvent;
    }

    volatile ISelection activeSelection;

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection nextSelection) {
        activeSelection = nextSelection;
        d.schedule(new Runnable() {

            @Override
            public void run() {
                if (activeSelection != nextSelection) {
                    // don't do anything
                    return;
                } else if (nextSelection instanceof IStructuredSelection) {
                    handleSelectionFromViewer(nextSelection);
                } else if (nextSelection instanceof ITextSelection && part instanceof JavaEditor) {
                    handleSelectionInEditor(part, nextSelection);
                }
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void handleSelectionFromViewer(final ISelection selection) {
        final Optional<IJavaElement> element = Selections.safeFirstElement(selection, IJavaElement.class);
        if (element.isPresent()) {
            final JavaElementSelectionLocation location = resolveSelectionLocationFromJavaElement(element.get());
            fireEventIfNew(element.get(), location, null);
        }
    }

    private void fireEventIfNew(final IJavaElement element, final JavaElementSelectionLocation location,
            final ASTNode selectedNode) {
        final JavaElementSelectionEvent event = new JavaElementSelectionEvent(element, location, selectedNode);
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
            final JavaElementSelectionLocation location = resolveSelectionLocationFromAstNode(node);
            fireEventIfNew(element.get(), location, node);
        }
    }
}
