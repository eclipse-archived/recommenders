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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Resolves the required information for a Java element selection.
 */
@SuppressWarnings("restriction")
final class JavaElementSelectionResolver {

    /**
     * Private constructor to avoid instantiation of helper class.
     */
    private JavaElementSelectionResolver() {
    }

    /**
     * @param part
     *            The workbench part in which the selection took place.
     * @param selection
     *            The selection event information.
     * @return The selection context resolved from the selection event.
     */
    public static IJavaElementSelection resolve(final IWorkbenchPart part, final ISelection selection) {
        Checks.ensureIsNotNull(part);
        IJavaElementSelection selectionContext = null;
        if (selection instanceof ITreeSelection) {
            selectionContext = resolveFromTreeSelection((ITreeSelection) selection);
        } else if (part instanceof JavaEditor && selection instanceof ITextSelection) {
            selectionContext = resolveFromEditor((JavaEditor) part, (ITextSelection) selection);
        }
        return selectionContext;
    }

    /**
     * @param selection
     *            The selection in a tree part - package explorer or outline.
     * @return The selection context resolved from the tree selection.
     */
    private static IJavaElementSelection resolveFromTreeSelection(final ITreeSelection selection) {
        IJavaElement javaElement = null;
        final Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IJavaElement) {
            javaElement = (IJavaElement) firstElement;
        }
        return javaElement == null ? null : new JavaElementSelection(javaElement);
    }

    /**
     * @param editor
     *            The editor in which the selection took place.
     * @param selection
     *            The text selection event information.
     * @return The selection context resolved from the editor selection.
     */
    private static IJavaElementSelection resolveFromEditor(final JavaEditor editor, final ITextSelection selection) {
        IJavaElement javaElement = null;
        final IEditorInput editorInput = editor.getEditorInput();
        final ITypeRoot root = (ITypeRoot) JavaUI.getEditorInputJavaElement(editorInput);
        try {
            final IJavaElement[] elements = root.codeSelect(selection.getOffset(), 0);
            if (elements.length > 0) {
                javaElement = elements[0];
            } else {
                javaElement = root.getElementAt(selection.getOffset());
            }
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
        return javaElement == null ? null : new JavaElementSelection(javaElement, selection.getOffset(), editor);
    }
}
