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

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Used to track the changes of the cursor in an editor.
 */
final class CursorListener implements MouseListener, KeyListener {

    private final InternalSelectionListener selectionListener;
    private final IWorkbenchWindow win;

    /**
     * @param selectionListener
     *            The parent listener to inform on cursor change event.
     * @param win
     *            The workbench window to whose editor the listener is
     *            registered to.
     */
    CursorListener(final InternalSelectionListener selectionListener, final IWorkbenchWindow win) {
        this.selectionListener = Checks.ensureIsNotNull(selectionListener);
        this.win = Checks.ensureIsNotNull(win);
    }

    /**
     * Called when a change of cursor position is observed. Notifies the parent
     * selection listener.
     */
    private void positionChanged() {
        final ISelection selection = win.getActivePage().getSelection();
        if (selection instanceof ITextSelection) {
            selectionListener.update(win.getActivePage().getActivePart(), selection);
        } else {
            throw new IllegalStateException("bla: " + selection);
        }
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        positionChanged();
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        // Not of interest for us.
    }

    @Override
    public void mouseDoubleClick(final MouseEvent event) {
        // Not of interest for us.
    }

    @Override
    public void mouseDown(final MouseEvent event) {
        // Not of interest for us.
    }

    @Override
    public void mouseUp(final MouseEvent event) {
        positionChanged();
    }

}
