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
package org.eclipse.recommenders.tests.commons.selection;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;

public final class TestSelectionListener implements IExtendedSelectionListener {

    private static IJavaElementSelection lastSelection;

    @Override
    public void selectionChanged(final IJavaElementSelection selection) {
        lastSelection = selection;
        // Always call toString to ensure no NPE will occur.
        selection.toString();
    }

    public static IJavaElementSelection getLastSelection() {
        return lastSelection;
    }

    @Override
    public void javaEditorCreated(final JavaEditor editor) {
    }

}
