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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;

/**
 * Resolves a Java element's code location type from an AST.
 */
final class JavaElementLocationResolver {

    private static Set<Integer> ignoredNodeTypes = new HashSet<Integer>();
    static {
        ignoredNodeTypes.add(ASTNode.SIMPLE_NAME);
        ignoredNodeTypes.add(ASTNode.SIMPLE_TYPE);
        ignoredNodeTypes.add(ASTNode.PARAMETERIZED_TYPE);
        ignoredNodeTypes.add(ASTNode.FIELD_ACCESS);
        ignoredNodeTypes.add(ASTNode.QUALIFIED_NAME);
        ignoredNodeTypes.add(ASTNode.METHOD_INVOCATION);
        ignoredNodeTypes.add(ASTNode.EXPRESSION_STATEMENT);
        ignoredNodeTypes.add(ASTNode.VARIABLE_DECLARATION_FRAGMENT);
        ignoredNodeTypes.add(ASTNode.VARIABLE_DECLARATION_STATEMENT);
        ignoredNodeTypes.add(ASTNode.CLASS_INSTANCE_CREATION);
        ignoredNodeTypes.add(ASTNode.RETURN_STATEMENT);
    }

    /**
     * Private constructor to avoid instantiation of helper class.
     */
    private JavaElementLocationResolver() {
    }

    /**
     * @param javaElement
     *            The Java element to identify the location for.
     * @param astNode
     *            AST node representing the selected java element.
     * @return The code location of the element represented by the AST node.
     */
    protected static JavaElementLocation resolveLocation(final IJavaElement javaElement, final ASTNode astNode) {
        if (astNode == null && javaElement instanceof IClassFile) {
            return JavaElementLocation.TYPE_DECLARATION;
        } else if (astNode == null && javaElement instanceof ResolvedBinaryMethod) {
            return JavaElementLocation.METHOD_DECLARATION;
        } else if (astNode == null) {
            return null;
        }
        final ASTNode locationNode = getLocationNode(astNode);
        return getLocationForNodeType(javaElement, astNode, locationNode);
    }

    /**
     * @param astNode
     *            AST node representing the selected Java element.
     * @return The type of the AST node indicating the Java element's location,
     *         e.g. "method declaration".
     */
    private static ASTNode getLocationNode(final ASTNode astNode) {
        ASTNode node = astNode;
        int nodeType = node.getNodeType();
        while (ignoredNodeTypes.contains(nodeType)) {
            node = node.getParent();
            nodeType = node.getNodeType();
        }
        return node;
    }

    /**
     * @param locationNodeType
     *            The type of the AST node indicating the element's location.
     * @param javaElement
     *            The Java element to identify the location for.
     * @return The {@link JavaElementLocation} for the given Java element and
     *         its location node type.
     */
    private static JavaElementLocation getLocationForNodeType(final IJavaElement javaElement, final ASTNode astNode,
            final ASTNode locationNode) {
        switch (locationNode.getNodeType()) {
        case ASTNode.BLOCK:
        case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
        case ASTNode.CAST_EXPRESSION:
            return JavaElementLocation.METHOD_BODY;
        case ASTNode.METHOD_DECLARATION:
            return JavaElementLocation.METHOD_DECLARATION;
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
            return getLocationForSingleVariable(locationNode);
        case ASTNode.FIELD_DECLARATION:
            return JavaElementLocation.FIELD_DECLARATION;
        case ASTNode.TYPE_DECLARATION:
            return getTypeDeclarationLocation(javaElement, astNode);
        case ASTNode.IMPORT_DECLARATION:
            return JavaElementLocation.IMPORT_DECLARATION;
        case ASTNode.PACKAGE_DECLARATION:
            return JavaElementLocation.PACKAGE_DECLARATION;
        default:
            return JavaElementLocation.METHOD_BODY;
            // throw new IllegalArgumentException("Could not find location for "
            // + locationNodeType);
        }
    }

    private static JavaElementLocation getLocationForSingleVariable(final ASTNode locationNode) {
        final int parentType = locationNode.getParent().getNodeType();
        return parentType == ASTNode.CATCH_CLAUSE ? JavaElementLocation.METHOD_BODY
                : JavaElementLocation.PARAMETER_DECLARATION;
    }

    /**
     * @param javaElement
     *            The Java element occurring in the type declaration.
     * @return The {@link JavaElementLocation} - the "new" type, part of
     *         "extends" or part of "implements" - as identified from the Java
     *         element type.
     */
    private static JavaElementLocation getTypeDeclarationLocation(final IJavaElement javaElement, final ASTNode astNode) {
        if (!(javaElement instanceof IType)) {
            return JavaElementLocation.TYPE_DECLARATION;
        }
        try {
            if (((IType) javaElement).isInterface()) {
                return JavaElementLocation.IMPLEMENTS_DECLARATION;
            }
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
        if (astNode.getNodeType() == ASTNode.TYPE_DECLARATION
                || astNode.getParent().getNodeType() == ASTNode.TYPE_DECLARATION
                && !(javaElement instanceof IPackageFragment)) {
            return JavaElementLocation.TYPE_DECLARATION;
        }
        return JavaElementLocation.EXTENDS_DECLARATION;
    }
}
