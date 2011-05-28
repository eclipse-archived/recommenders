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

final class CursorListener implements MouseListener, KeyListener {

    private final SelectionListener selectionListener;
    private final IWorkbenchWindow win;

    CursorListener(final SelectionListener selectionListener, final IWorkbenchWindow win) {
        this.selectionListener = Checks.ensureIsNotNull(selectionListener);
        this.win = Checks.ensureIsNotNull(win);
    }

    private void positionChanged() {
        final ISelection selection = win.getActivePage().getSelection();
        if (selection instanceof ITextSelection) {
            selectionListener.update(win.getActivePage().getActivePart(), selection);
        }
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        positionChanged();
    }

    @Override
    public void keyReleased(final KeyEvent event) {
    }

    @Override
    public void mouseDoubleClick(final MouseEvent event) {
    }

    @Override
    public void mouseDown(final MouseEvent event) {
    }

    @Override
    public void mouseUp(final MouseEvent event) {
        positionChanged();
    }

}
