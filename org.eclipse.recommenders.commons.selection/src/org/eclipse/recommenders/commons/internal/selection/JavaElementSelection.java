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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.annotations.Testing;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * Contains all required information about the user's selection of a java
 * element in the perspective (e.g. Editor, Package Explorer, Outline, ...).
 */
@SuppressWarnings("restriction")
public final class JavaElementSelection implements IJavaElementSelection {

    private final IJavaElement javaElement;
    private final int invocationOffset;
    private JavaEditor editor;

    private JavaElementLocation cachedLocation;
    private ITypeRoot compilationUnit;
    private ASTNode cachedAstNode;

    /**
     * @param javaElement
     *            The selected Java element.
     */
    public JavaElementSelection(final IJavaElement javaElement) {
        this.javaElement = Checks.ensureIsNotNull(javaElement);
        invocationOffset = -1;
    }

    /**
     * @param javaElement
     *            The selected Java element.
     * @param invocationOffset
     *            The offset of the selection in the code.
     * @param editor
     *            The Java editor in which the selection took place.
     */
    public JavaElementSelection(final IJavaElement javaElement, final int invocationOffset, final JavaEditor editor) {
        this.javaElement = Checks.ensureIsNotNull(javaElement);
        this.invocationOffset = invocationOffset;
        this.editor = Checks.ensureIsNotNull(editor);
    }

    @Override
    public IJavaElement getJavaElement() {
        return javaElement;
    }

    @Override
    public JavaElementLocation getElementLocation() {
        if (cachedLocation == null) {
            cachedLocation = JavaElementLocationResolver.resolveLocation(javaElement, getAstNode());
        }
        return cachedLocation;
    }

    @Override
    public int getInvocationOffset() {
        return invocationOffset;
    }

    @Override
    public final ITypeRoot getCompilationUnit() {
        if (compilationUnit == null && editor != null) {
            compilationUnit = Checks.cast(EditorUtility.getEditorInputJavaElement(editor, false));
        }
        return compilationUnit;
    }

    @Override
    public ASTNode getAstNode() {
        if (cachedAstNode == null && getCompilationUnit() != null) {
            cachedAstNode = AstNodeResolver.resolveNode(getCompilationUnit(), invocationOffset);
        }
        return cachedAstNode;
    }

    @Override
    public JavaEditor getEditor() {
        return editor;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof IJavaElementSelection)) {
            return false;
        }
        final IJavaElementSelection selection = (IJavaElementSelection) object;
        if (getElementLocation() != selection.getElementLocation()) {
            return false;
        }
        if (!getJavaElement().equals(selection.getJavaElement())) {
            return false;
        }
        if (getEditor() == null) {
            return selection.getEditor() == null;
        }
        return getEditor().equals(selection.getEditor());
    }

    @Testing
    @Override
    public String toString() {
        final ToStringHelper string = Objects.toStringHelper(this).add("\n\nJavaElementClass",
                javaElement == null ? null : javaElement.getClass() + " / " + javaElement.getHandleIdentifier());
        string.add("\nElementLocation", getElementLocation());
        if (getAstNode() != null) {
            final ASTNode astNode = getAstNode();
            string.add("\nAstNode", astNode.getNodeType() + " ("
                    + ASTNode.nodeClassForType(astNode.getNodeType()).getSimpleName() + ")");
        }
        string.add("\n\nInvocationOffset", invocationOffset + "\n\n");
        return string.toString();
    }
}
