/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.internal.rcp.l10n.LogMessages.*;
import static org.eclipse.recommenders.rcp.JavaElementSelectionEvent.JavaElementSelectionLocation.*;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findTypeRoot;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent.JavaElementSelectionLocation;
import org.eclipse.ui.IEditorPart;

import com.google.common.base.Optional;

/**
 * Utility class that resolves a selected java element from editor selection or structured selection.
 */
@SuppressWarnings("restriction")
public final class JavaElementSelections {

    private JavaElementSelections() {
        // Not meant to be instantiated
    }

    @SuppressWarnings("serial")
    private static final Map<StructuralPropertyDescriptor, JavaElementSelectionLocation> MAPPING = new HashMap<StructuralPropertyDescriptor, JavaElementSelectionLocation>() {
        {
            put(CompilationUnit.IMPORTS_PROPERTY, TYPE_DECLARATION);
            put(CompilationUnit.PACKAGE_PROPERTY, TYPE_DECLARATION);
            put(CompilationUnit.TYPES_PROPERTY, TYPE_DECLARATION);

            put(TypeDeclaration.BODY_DECLARATIONS_PROPERTY, TYPE_DECLARATION);
            put(TypeDeclaration.INTERFACE_PROPERTY, TYPE_DECLARATION);
            put(TypeDeclaration.JAVADOC_PROPERTY, TYPE_DECLARATION);
            put(TypeDeclaration.MODIFIERS2_PROPERTY, TYPE_DECLARATION);
            put(TypeDeclaration.NAME_PROPERTY, TYPE_DECLARATION);
            put(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, TYPE_DECLARATION_EXTENDS);
            put(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY, TYPE_DECLARATION_IMPLEMENTS);
            put(TypeDeclaration.TYPE_PARAMETERS_PROPERTY, UNKNOWN);

            put(MethodDeclaration.BODY_PROPERTY, METHOD_BODY);
            put(MethodDeclaration.CONSTRUCTOR_PROPERTY, METHOD_DECLARATION);
            put(MethodDeclaration.JAVADOC_PROPERTY, METHOD_DECLARATION);
            put(MethodDeclaration.MODIFIERS2_PROPERTY, METHOD_DECLARATION);
            put(MethodDeclaration.NAME_PROPERTY, METHOD_DECLARATION);
            put(MethodDeclaration.PARAMETERS_PROPERTY, METHOD_DECLARATION_PARAMETER);
            put(MethodDeclaration.RETURN_TYPE2_PROPERTY, METHOD_DECLARATION_RETURN);
            put(MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY, METHOD_DECLARATION_THROWS);
            put(MethodDeclaration.TYPE_PARAMETERS_PROPERTY, UNKNOWN);

            put(Initializer.BODY_PROPERTY, METHOD_BODY);
            put(Initializer.MODIFIERS2_PROPERTY, METHOD_DECLARATION);

            put(FieldDeclaration.FRAGMENTS_PROPERTY, FIELD_DECLARATION_INITIALIZER);
            put(VariableDeclarationFragment.NAME_PROPERTY, FIELD_DECLARATION);
            put(FieldDeclaration.TYPE_PROPERTY, FIELD_DECLARATION);
            put(FieldDeclaration.JAVADOC_PROPERTY, FIELD_DECLARATION);
            put(FieldDeclaration.MODIFIERS2_PROPERTY, FIELD_DECLARATION);
        }
    };

    /**
     * Returns the {@link IJavaElement} at the current offset or {@link Optional#absent()} if resolving fails.
     */
    public static Optional<IJavaElement> resolveJavaElementFromEditor(final IEditorPart editor,
            final ITextSelection selection) {
        ensureIsNotNull(editor);
        ensureIsNotNull(selection);
        if (!isValidSelection(selection)) {
            return absent();
        }
        if (editor instanceof JavaEditor) {
            final JavaEditor javaEditor = (JavaEditor) editor;
            return resolveJavaElementFromEditor(javaEditor, selection.getOffset());
        }
        return absent();
    }

    private static boolean isValidSelection(final ITextSelection selection) {
        return selection.getOffset() != -1;
    }

    /**
     * Returns the {@link IJavaElement} at the given offset in the editor.
     *
     */
    public static Optional<IJavaElement> resolveJavaElementFromEditor(final JavaEditor editor, final int offset) {
        ensureIsNotNull(editor);
        ITypeRoot root = findTypeRoot(editor).orNull();
        if (root != null && root.exists()) {
            return resolveJavaElementFromTypeRootInEditor(root, offset);
        }
        return absent();
    }

    /**
     * Returns the {@link IJavaElement} at the given offset. If no {@link IJavaElement} is selected, the innermost
     * enclosing {@link IJavaElement} is returned (e.g., the declaring method or type). If both selection resolutions
     * fail, {@link Optional#absent()} is returned.
     */
    public static Optional<IJavaElement> resolveJavaElementFromTypeRootInEditor(final ITypeRoot root,
            final int offset) {
        ensureIsNotNull(root);
        try {
            if (isInvalidSelection(root, offset)) {
                return absent();
            }

            // try resolve elements at current offset
            final IJavaElement[] elements = root.codeSelect(offset, 0);
            if (elements.length > 0) {
                // return java element under cursor/selection start
                return of(elements[0]);
            } else {
                // XXX MB: decided against selection changes because these
                // frequent changes were too disturbing
                return absent();
                // ignore that for a while:

                // // if no java element has been selected, return the innermost
                // Java element enclosing a given offset.
                // // This might evaluate to null.
                // IJavaElement enclosingElement = root.getElementAt(offset);
                // if (enclosingElement == null) {
                // // selection occurred in empty space somewhere before the
                // type declaration.
                // // return type-root then.
                // enclosingElement = root;
                // }
                // return of(enclosingElement);
            }
        } catch (final Exception e) {
            // actually, these can happen when using snipmatch's in-editor completion.
            // fractions of seconds seem potentially to lead to this exception, thus, we swallow them here.
            if (!isInvalidSelection(root, offset)) {
                log(FAILED_TO_RESOLVE_SELECTION, root.getHandleIdentifier(), offset, e);
            }
            return absent();
        }
    }

    private static boolean isInvalidSelection(ITypeRoot root, final int offset) {
        try {
            if (!root.exists()) {
                return true;
            }
            // check whether the type root is part of an package fragment root. If not, it's an invalid selection and
            // all resolutions are likely to fail. Thus, return true (=invalid):
            IJavaElement ancestor = root.getAncestor(IJavaProject.PACKAGE_FRAGMENT_ROOT);
            if (!ancestor.exists()) {
                return true;
            }
            ISourceRange range = root.getSourceRange();
            return range == null || offset < 0 || offset > range.getLength();
        } catch (Exception e) {
            log(EXCEPTION_WHILE_CHECKING_OFFSETS, e);
            return false;
        }
    }

    public static JavaElementSelectionLocation resolveSelectionLocationFromAst(final CompilationUnit astRoot,
            final int offset) {
        ensureIsNotNull(astRoot);
        final ASTNode selectedNode = NodeFinder.perform(astRoot, offset, 0);
        if (selectedNode == null) {
            // this *should* never happen but it *can* happen...
            return JavaElementSelectionLocation.UNKNOWN;
        }
        return resolveSelectionLocationFromAstNode(selectedNode);
    }

    public static JavaElementSelectionLocation resolveSelectionLocationFromAstNode(final ASTNode node) {
        if (node == null) {
            return JavaElementSelectionLocation.UNKNOWN;
        }

        // handle a direct selection on a declaration node, i.e., the users
        // select a whitespace as in
        // "public $ void do(){}":
        // TODO Review: create second(?) mapping
        switch (node.getNodeType()) {
        case ASTNode.COMPILATION_UNIT:
        case ASTNode.TYPE_DECLARATION:
            return TYPE_DECLARATION;
        case ASTNode.METHOD_DECLARATION:
        case ASTNode.INITIALIZER:
            return METHOD_DECLARATION;
        case ASTNode.FIELD_DECLARATION:
            return FIELD_DECLARATION;
        default:
        }

        return resolveSelectionLocationFromNonMemberDeclarationNode(node);
    }

    /**
     * some inner node that is not a method, a type or a field declaration node...
     */
    private static JavaElementSelectionLocation resolveSelectionLocationFromNonMemberDeclarationNode(ASTNode node) {
        // deal with special case that no parent exists: for instance, if empty
        // spaces before the package declaration
        // are selected, we translate this to type declaration:
        ASTNode parent = node.getParent();
        if (parent == null) {
            return JavaElementSelectionLocation.TYPE_DECLARATION;
        }
        // we have a child node selected. Let's figure out which location this
        // translates best:
        while (node != null) {
            final StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
            switch (parent.getNodeType()) {
            case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
                if (isVariableNameSelectionInFieldDeclaration(parent, locationInParent)) {
                    return FIELD_DECLARATION;
                }
                break;
            case ASTNode.COMPILATION_UNIT:
            case ASTNode.TYPE_DECLARATION:
            case ASTNode.METHOD_DECLARATION:
            case ASTNode.FIELD_DECLARATION:
            case ASTNode.INITIALIZER:
                return mapLocationInParent(locationInParent);
            default:
                break;
            }
            node = parent;
            parent = parent.getParent();
        }
        return JavaElementSelectionLocation.UNKNOWN;
    }

    private static boolean isVariableNameSelectionInFieldDeclaration(final ASTNode parent,
            final StructuralPropertyDescriptor locationInParent) {
        final ASTNode superparent = parent.getParent();
        return superparent instanceof FieldDeclaration && VariableDeclarationFragment.NAME_PROPERTY == locationInParent;
    }

    private static JavaElementSelectionLocation mapLocationInParent(
            final StructuralPropertyDescriptor locationInParent) {
        final JavaElementSelectionLocation res = MAPPING.get(locationInParent);
        return res != null ? res : JavaElementSelectionLocation.UNKNOWN;
    }

    // TODO Review: rename method
    public static JavaElementSelectionLocation resolveSelectionLocationFromJavaElement(final IJavaElement element) {
        ensureIsNotNull(element);

        switch (element.getElementType()) {
        case IJavaElement.CLASS_FILE:
        case IJavaElement.COMPILATION_UNIT:
        case IJavaElement.PACKAGE_DECLARATION:
        case IJavaElement.IMPORT_DECLARATION:
        case IJavaElement.IMPORT_CONTAINER:
        case IJavaElement.TYPE:
            return TYPE_DECLARATION;
        case IJavaElement.METHOD:
        case IJavaElement.INITIALIZER:
            return METHOD_DECLARATION;
        case IJavaElement.FIELD:
            return FIELD_DECLARATION;
        case IJavaElement.LOCAL_VARIABLE:
            // shouldn't happen in a viewer selection, right?
            return METHOD_BODY;
        case IJavaElement.JAVA_MODEL:
        case IJavaElement.PACKAGE_FRAGMENT:
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        case IJavaElement.ANNOTATION:
        default:
            return JavaElementSelectionLocation.UNKNOWN;
        }
    }
}
