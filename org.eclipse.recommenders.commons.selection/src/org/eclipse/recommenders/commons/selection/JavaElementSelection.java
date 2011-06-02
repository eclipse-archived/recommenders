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

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.commons.internal.selection.AstNodeResolver;
import org.eclipse.recommenders.commons.internal.selection.JavaElementLocationResolver;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.annotations.Testing;

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
    private JavaContentAssistInvocationContext cachedContext;
    private ASTNode cachedAstNode;

    /**
     * @param javaElement
     *            The selected Java element.
     */
    public JavaElementSelection(final IJavaElement javaElement) {
        this.javaElement = javaElement;
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
        this.javaElement = javaElement;
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
    public JavaContentAssistInvocationContext getInvocationContext() {
        if (cachedContext == null && editor != null) {
            cachedContext = new JavaContentAssistInvocationContext(editor.getViewer(), invocationOffset, editor);
        }
        return cachedContext;
    }

    @Override
    public ASTNode getAstNode() {
        if (cachedAstNode == null) {
            cachedAstNode = AstNodeResolver.resolveNode(getInvocationContext());
        }
        return cachedAstNode;
    }

    @Testing
    @Override
    public String toString() {
        final ToStringHelper string = Objects.toStringHelper(this).add("\n\nJavaElementClass",
                javaElement == null ? null : javaElement.getClass());
        string.add("\nElementLocation", getElementLocation());
        if (getAstNode() != null) {
            final ASTNode astNode = getAstNode();
            string.add("\nAstNode", astNode.getNodeType() + " ("
                    + ASTNode.nodeClassForType(astNode.getNodeType()).getSimpleName() + ")");
            string.add(
                    "\nAstNodeParent",
                    astNode.getParent().getNodeType() + " ("
                            + ASTNode.nodeClassForType(astNode.getParent().getNodeType()).getSimpleName() + ")");
        }
        string.add("\n\nInvocationOffset", invocationOffset + "\n\n");

        final JavaContentAssistInvocationContext context = getInvocationContext();
        if (context != null) {
            string.add("ExpectedType", context.getExpectedType());
            final CompletionContext coreContext = context.getCoreContext();
            if (coreContext != null && coreContext.getToken() != null) {
                string.add("\nCoreContextToken", String.valueOf(coreContext.getToken()) + "\n\n");
            }
        }
        return string.toString();
    }
}
