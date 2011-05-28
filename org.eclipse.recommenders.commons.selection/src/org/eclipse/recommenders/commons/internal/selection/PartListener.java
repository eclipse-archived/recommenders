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

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

@SuppressWarnings("restriction")
final class PartListener implements IPartListener {

    private final CursorListener cursorListener;

    public PartListener(final CursorListener listener) {
        cursorListener = listener;
    }

    private void addListeners(final IWorkbenchPart part) {
        if (part instanceof JavaEditor) {
            final JavaEditor editor = (JavaEditor) part;
            final StyledText text = (StyledText) editor.getAdapter(Control.class);
            text.addKeyListener(cursorListener);
            text.addMouseListener(cursorListener);
        }
    }

    @Override
    public void partActivated(final IWorkbenchPart part) {
        addListeners(part);
    }

    @Override
    public void partBroughtToTop(final IWorkbenchPart part) {
        // Not of interest for us.
    }

    @Override
    public void partClosed(final IWorkbenchPart part) {
        // Not of interest for us.
    }

    @Override
    public void partDeactivated(final IWorkbenchPart part) {
        // Not of interest for us.
    }

    @Override
    public void partOpened(final IWorkbenchPart part) {
        // Not of interest for us.
    }

}
