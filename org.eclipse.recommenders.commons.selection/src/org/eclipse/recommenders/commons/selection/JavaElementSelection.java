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
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.internal.selection.AstNodeResolver;
import org.eclipse.recommenders.commons.internal.selection.ElementLocationResolver;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.ui.IEditorPart;

public final class JavaElementSelection implements IJavaElementSelection {

    private static final AstNodeResolver ASTNODERESOLVER = new AstNodeResolver();
    private static final ElementLocationResolver ELEMENTLOCATIONRESOLVER = new ElementLocationResolver();

    private final ISelection selection;
    private final IJavaElement javaElement;

    private ElementLocation cachedElementLocation;
    private JavaContentAssistInvocationContext cachedContext;
    private ASTNode cachedAstNode;

    private ITextViewer viewer;
    private IEditorPart editor;

    public JavaElementSelection(final ISelection selection, final IJavaElement javaElement) {
        this.selection = Checks.ensureIsNotNull(selection);
        this.javaElement = javaElement;
    }

    public JavaElementSelection(final ISelection selection, final IJavaElement javaElement, final ITextViewer viewer,
            final IEditorPart editor) {
        this.selection = Checks.ensureIsNotNull(selection);
        this.javaElement = javaElement;

        this.viewer = Checks.ensureIsNotNull(viewer);
        this.editor = Checks.ensureIsNotNull(editor);
    }

    @Override
    public IJavaElement getJavaElement() {
        return javaElement;
    }

    @Override
    public ElementLocation getElementLocation() {
        if (cachedElementLocation == null) {
            cachedElementLocation = ELEMENTLOCATIONRESOLVER.resolve(getAstNode());
        }
        return cachedElementLocation;
    }

    @Override
    public JavaContentAssistInvocationContext getInvocationContext() {
        if (cachedContext == null && viewer != null) {
            cachedContext = new JavaContentAssistInvocationContext(viewer, getInvocationOffset(), editor);
        }
        return cachedContext;
    }

    @Override
    public ASTNode getAstNode() {
        if (cachedAstNode == null) {
            cachedAstNode = ASTNODERESOLVER.resolve(getInvocationContext());
        }
        return cachedAstNode;
    }

    private int getInvocationOffset() {
        return selection instanceof TextSelection ? ((TextSelection) selection).getOffset() : -1;
    }

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
        string.add("\n\nInvocationOffset", getInvocationOffset() + "\n\n");

        final JavaContentAssistInvocationContext context = getInvocationContext();
        if (context != null) {
            try {
                string.add("ExpectedType", context.getExpectedType()).add("\n\tIdentifierPrefix",
                        context.computeIdentifierPrefix());
            } catch (final BadLocationException e) {
                throw new IllegalStateException(e);
            }
            final CompletionContext coreContext = context.getCoreContext();
            if (coreContext != null && coreContext.getToken() != null) {
                string.add("\nCoreContextToken", String.valueOf(coreContext.getToken()) + "\n\n");
            }
        }
        return string.toString();
    }
}
