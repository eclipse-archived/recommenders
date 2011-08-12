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
package org.eclipse.recommenders.commons.internal.selection;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementSelectionResolver;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Listens for selection events in the workbench and notifies external
 * listeners.
 */
@SuppressWarnings("restriction")
final class InternalSelectionListener implements ISelectionListener {

    private ISelection lastSelection;
    private final Set<IExtendedSelectionListener> externalListeners = new HashSet<IExtendedSelectionListener>();

    /**
     * @param part
     *            The workbench part in which the selection took place.
     * @param selection
     *            The selection event information to send to the external
     *            listeners.
     */
    protected void update(final IWorkbenchPart part, final ISelection selection) {
        if (!selection.equals(lastSelection)) {
            lastSelection = selection;

            final IJavaElementSelection elementSelection = JavaElementSelectionResolver.resolve(part, selection);
            notifyListeners(elementSelection);
        }
    }

    /**
     * @param selection
     *            Information about the selection Java element.
     */
    private void notifyListeners(final IJavaElementSelection selection) {
        if (selection != null) {
            for (final IExtendedSelectionListener externalListener : externalListeners) {
                externalListener.selectionChanged(selection);
            }
        }
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (!(part instanceof JavaEditor)) {
            update(part, selection);
        }
    }

    protected void javaEditorCreated(final JavaEditor editor) {
        for (final IExtendedSelectionListener externalListener : externalListeners) {
            externalListener.javaEditorCreated(editor);
        }
    }

    /**
     * @param listener
     *            External listener to be notified about element selection.
     */
    protected void addExternalListener(final IExtendedSelectionListener listener) {
        externalListeners.add(listener);
    }
}
