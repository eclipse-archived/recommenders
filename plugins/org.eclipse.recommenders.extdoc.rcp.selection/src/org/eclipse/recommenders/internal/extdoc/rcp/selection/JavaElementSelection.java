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
package org.eclipse.recommenders.internal.extdoc.rcp.selection;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.JavaElementLocation;
import org.eclipse.recommenders.utils.Checks;

import com.google.common.base.Preconditions;

/**
 * Contains all required information about the user's selection of a java
 * element in the perspective (e.g. Editor, Package Explorer, Outline, ...).
 */
@SuppressWarnings("restriction")
public final class JavaElementSelection implements IJavaElementSelection {

    private final IJavaElement javaElement;
    private JavaElementLocation location;
    private JavaEditor editor;
    private final int invocationOffset;

    private ITypeRoot compilationUnit;
    private ASTNode astNode;

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
        this.editor = editor;
    }

    @Override
    public IJavaElement getJavaElement() {
        return javaElement;
    }

    @Override
    public JavaElementLocation getElementLocation() {
        if (location == null) {
            location = JavaElementLocationResolver.resolveLocation(javaElement, getAstNode());
        }
        return location;
    }

    @Override
    public JavaEditor getEditor() {
        return editor;
    }

    @Override
    public int getInvocationOffset() {
        return invocationOffset;
    }

    @Override
    public ITypeRoot getCompilationUnit() {
        if (compilationUnit == null && editor != null) {
            compilationUnit = Checks.cast(EditorUtility.getEditorInputJavaElement(editor, false));
        }
        return compilationUnit;
    }

    @Override
    public ASTNode getAstNode() {
        if (astNode == null && getCompilationUnit() != null) {
            final ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setResolveBindings(true);
            parser.setSource(Preconditions.checkNotNull(compilationUnit));
            final ASTNode astRoot = parser.createAST(null);
            astNode = NodeFinder.perform(astRoot, invocationOffset, 0);
        }
        return astNode;
    }

    @Override
    public IJavaElementSelection copy(final IJavaElement element) {
        return new JavaElementSelection(element, invocationOffset, editor);
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

    @Override
    public int hashCode() {
        int sum = 0;
        sum += getJavaElement() == null ? 0 : getJavaElement().hashCode();
        sum += getElementLocation() == null ? 0 : getElementLocation().hashCode();
        sum += getEditor() == null ? 0 : getEditor().hashCode();
        return sum;
    }
}
