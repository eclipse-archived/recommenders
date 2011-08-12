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
package org.eclipse.recommenders.commons.selection;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * Listener for Java element selections.
 */
@SuppressWarnings("restriction")
public interface IExtendedSelectionListener {

    /**
     * @param selection
     *            The context information for a new Java element selection.
     */
    void selectionChanged(IJavaElementSelection selection);

    void javaEditorCreated(JavaEditor editor);

}
