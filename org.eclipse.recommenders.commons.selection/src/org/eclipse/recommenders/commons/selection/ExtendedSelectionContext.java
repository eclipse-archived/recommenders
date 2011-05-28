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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

public final class ExtendedSelectionContext {

    private final IWorkbenchPart part;
    private final ISelection selection;
    private final IJavaElement javaElement;

    private ITextViewer viewer;
    private IEditorPart editor;
    private JavaContentAssistInvocationContext cachedContext;
    private ASTNode cachedAstNode;

    protected ExtendedSelectionContext(final IWorkbenchPart part, final ISelection selection,
            final IJavaElement javaElement) {
        this.part = Checks.ensureIsNotNull(part);
        this.selection = Checks.ensureIsNotNull(selection);
        this.javaElement = javaElement;
    }

    protected ExtendedSelectionContext(final IWorkbenchPart part, final ISelection selection,
            final IJavaElement javaElement, final ITextViewer viewer, final IEditorPart editor) {
        this.part = Checks.ensureIsNotNull(part);
        this.selection = Checks.ensureIsNotNull(selection);
        this.javaElement = javaElement;

        this.viewer = Checks.ensureIsNotNull(viewer);
        this.editor = Checks.ensureIsNotNull(editor);
    }

    public IWorkbenchPart getPart() {
        return part;
    }

    public ISelection getSelection() {
        return selection;
    }

    public IJavaElement getJavaElement() {
        return javaElement;
    }

    public int getInvocationOffset() {
        return selection instanceof TextSelection ? ((TextSelection) selection).getOffset() : -1;
    }

    public JavaContentAssistInvocationContext getInvocationContext() {
        if (cachedContext == null && viewer != null) {
            cachedContext = new JavaContentAssistInvocationContext(viewer, getInvocationOffset(), editor);
        }
        return cachedContext;
    }

    public ASTNode getAstNode() {
        if (cachedAstNode == null && getInvocationContext() != null) {
            final ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setResolveBindings(true);
            parser.setSource(getInvocationContext().getCompilationUnit());
            cachedAstNode = NodeFinder.perform(parser.createAST(null), getInvocationOffset(), 0);
            int nodeType = cachedAstNode.getNodeType();
            while (nodeType == ASTNode.SIMPLE_NAME || nodeType == ASTNode.SIMPLE_TYPE
                    || nodeType == ASTNode.SINGLE_VARIABLE_DECLARATION || nodeType == ASTNode.QUALIFIED_NAME
                    || nodeType == ASTNode.METHOD_INVOCATION || nodeType == ASTNode.EXPRESSION_STATEMENT
                    || nodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT
                    || nodeType == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
                cachedAstNode = cachedAstNode.getParent();
                nodeType = cachedAstNode.getNodeType();
            }
        }
        return cachedAstNode;
    }

    @Override
    public String toString() {
        final ToStringHelper string = Objects.toStringHelper(this)
                .add("\n\tPart", part.getTitle() + " (" + part.getClass() + ")")
                .add("\n\tSelection", selection.getClass())
                // .add("\n\tJavaElement", javaElement == null ? null :
                // javaElement + " (" + javaElement.getClass() + ")")
                .add("\n\tJavaElementClass", javaElement == null ? null : javaElement.getClass());
        if (javaElement != null && getAstNode() != null) {
            final ASTNode astNode = getAstNode();
            string.add("\n\tAstNode", astNode.getNodeType() + " ("
                    + ASTNode.nodeClassForType(astNode.getNodeType()).getSimpleName() + ")");
            string.add(
                    "\n\tAstNodeParent",
                    astNode.getParent().getNodeType() + " ("
                            + ASTNode.nodeClassForType(astNode.getParent().getNodeType()).getSimpleName() + ")");
        }
        string.add("\n\n\tInvocationOffset", getInvocationOffset());

        final JavaContentAssistInvocationContext context = getInvocationContext();
        if (context != null) {
            try {
                string.add("\n\tExpectedType", context.getExpectedType()).add("\n\tIdentifierPrefix",
                        context.computeIdentifierPrefix());
            } catch (final BadLocationException e) {
                throw new IllegalStateException(e);
            }
            final CompletionContext coreContext = context.getCoreContext();
            if (coreContext != null && coreContext.getToken() != null) {
                string.add("\n\tCoreContextToken", String.valueOf(coreContext.getToken()) + "\n");
            }
        }
        return string.toString();
    }
}
