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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;

public final class SelectionResolver {

    private SelectionResolver() {
    }

    public static IJavaElement resolveJavaElement(final IEditorInput editorInput, final int offset) {
        final ITypeRoot root = (ITypeRoot) JavaUI.getEditorInputJavaElement(editorInput);
        try {
            final IJavaElement[] elements = root.codeSelect(offset, 0);
            return elements.length > 0 ? elements[0] : root.getElementAt(offset);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

}
