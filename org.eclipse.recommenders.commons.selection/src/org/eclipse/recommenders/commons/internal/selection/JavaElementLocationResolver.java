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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;

/**
 * Resolves a Java element's code location type from an AST.
 */
@SuppressWarnings("restriction")
final class JavaElementLocationResolver {

    private static Set<Integer> ignoredNodeTypes = new HashSet<Integer>();
    static {
        ignoredNodeTypes.add(ASTNode.SIMPLE_NAME);
        ignoredNodeTypes.add(ASTNode.SIMPLE_TYPE);
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
        if (astNode == null) {
            return null;
        }
        final int locationNodeType = getLocationNodeType(astNode);
        return getLocationForNodeType(locationNodeType, javaElement);
    }

    /**
     * @param astNode
     *            AST node representing the selected Java element.
     * @return The type of the AST node indicating the Java element's location,
     *         e.g. "method declaration".
     */
    private static int getLocationNodeType(final ASTNode astNode) {
        ASTNode node = astNode;
        Integer nodeType = node.getNodeType();
        while (ignoredNodeTypes.contains(nodeType)) {
            node = node.getParent();
            nodeType = node.getNodeType();
        }
        return nodeType;
    }

    /**
     * @param locationNodeType
     *            The type of the AST node indicating the element's location.
     * @param javaElement
     *            The Java element to identify the location for.
     * @return The {@link JavaElementLocation} for the given Java element and
     *         its location node type.
     */
    private static JavaElementLocation getLocationForNodeType(final int locationNodeType, final IJavaElement javaElement) {
        switch (locationNodeType) {
        case ASTNode.BLOCK:
            return JavaElementLocation.BLOCK;
        case ASTNode.METHOD_DECLARATION:
            return JavaElementLocation.METHOD_DECLARATION;
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
            return JavaElementLocation.METHOD_DECLARATION_PARAMETER;
        case ASTNode.FIELD_DECLARATION:
            return JavaElementLocation.FIELD_DECLARATION;
        case ASTNode.TYPE_DECLARATION:
            return getTypeDeclarationLocation(javaElement);
        case ASTNode.IMPORT_DECLARATION:
            return JavaElementLocation.IMPORT_DECLARATION;
        case ASTNode.PACKAGE_DECLARATION:
            return JavaElementLocation.PACKAGE_DECLARATION;
        default:
            throw new IllegalArgumentException("Could not find location for " + locationNodeType);
        }
    }

    /**
     * @param javaElement
     *            The Java element occurring in the type declaration.
     * @return The {@link JavaElementLocation} - the "new" type, part of
     *         "extends" or part of "implements" - as identified from the Java
     *         element type.
     */
    private static JavaElementLocation getTypeDeclarationLocation(final IJavaElement javaElement) {
        if (javaElement instanceof SourceType) {
            return JavaElementLocation.TYPE_DECLARATION;
        } else {
            try {
                if (!((BinaryType) javaElement).isInterface()) {
                    return JavaElementLocation.TYPE_DECLARATION_EXTENDS;
                }
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return JavaElementLocation.TYPE_DECLARATION_IMPLEMENTS;
    }
}
