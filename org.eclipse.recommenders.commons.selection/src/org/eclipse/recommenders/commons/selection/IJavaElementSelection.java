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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.ui.IEditorPart;

/**
 * Contains all required information about the user's selection of a java
 * element in the perspective (e.g. Editor, Package Explorer, Outline, ...).
 */
public interface IJavaElementSelection {

    /**
     * @return The selected java element.
     */
    IJavaElement getJavaElement();

    /**
     * @return The location type inside the compilation unit (e.g.
     *         "method declaration" or "block") if the selection occurs in the
     *         editor.
     */
    JavaElementLocation getElementLocation();

    int getInvocationOffset();

    ITypeRoot getCompilationUnit();

    /**
     * @return The selected java element's AST node.
     */
    ASTNode getAstNode();

    IEditorPart getEditor();

}
