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
import org.eclipse.recommenders.commons.selection.JavaElementSelection;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;

@SuppressWarnings("restriction")
final class SelectionContextResolver {

    public IJavaElementSelection resolve(final IWorkbenchPart part, final ISelection selection)
            throws JavaModelException {
        Checks.ensureIsNotNull(part);
        IJavaElementSelection selectionContext = null;
        if (selection instanceof ITreeSelection) {
            selectionContext = resolveFromTreeSelection((ITreeSelection) selection);
        } else if (part instanceof JavaEditor && selection instanceof ITextSelection) {
            selectionContext = resolveFromEditor((JavaEditor) part, (ITextSelection) selection);
        }
        return selectionContext;
    }

    private IJavaElementSelection resolveFromTreeSelection(final ITreeSelection selection) throws JavaModelException {
        IJavaElement javaElement = null;
        final Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IJavaElement) {
            javaElement = (IJavaElement) firstElement;
        }
        return new JavaElementSelection(selection, javaElement);
    }

    private IJavaElementSelection resolveFromEditor(final JavaEditor part, final ITextSelection selection)
            throws JavaModelException {
        IJavaElement javaElement = null;
        final IEditorInput editorInput = part.getEditorInput();
        final ITypeRoot root = (ITypeRoot) JavaUI.getEditorInputJavaElement(editorInput);
        final IJavaElement[] elements = root.codeSelect(selection.getOffset(), 0);
        if (elements.length > 0) {
            javaElement = elements[0];
        }
        return new JavaElementSelection(selection, javaElement, part.getViewer(), part);
    }
}
