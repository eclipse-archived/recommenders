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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.recommenders.commons.selection.ElementLocation;

/**
 * Resolves a Java element's code location type from an AST.
 */
@SuppressWarnings("restriction")
public final class ElementLocationResolver {

    /**
     * Private constructor to avoid instantiation of helper class.
     */
    private ElementLocationResolver() {
    }

    /**
     * @param astNode
     *            AST node representing the selected java element.
     * @param javaElement
     *            The Java element to identify the location for.
     * @return The code location of the element represented by the AST node.
     */
    public static ElementLocation resolve(final ASTNode astNode, final IJavaElement javaElement) {
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
        int nodeType = node.getNodeType();
        while (nodeType == ASTNode.SIMPLE_NAME || nodeType == ASTNode.SIMPLE_TYPE || nodeType == ASTNode.FIELD_ACCESS
                || nodeType == ASTNode.QUALIFIED_NAME || nodeType == ASTNode.METHOD_INVOCATION
                || nodeType == ASTNode.EXPRESSION_STATEMENT || nodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT
                || nodeType == ASTNode.VARIABLE_DECLARATION_STATEMENT || nodeType == ASTNode.CLASS_INSTANCE_CREATION
                || nodeType == ASTNode.RETURN_STATEMENT) {
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
     * @return The {@link ElementLocation} for the given Java element and its
     *         location node type.
     */
    private static ElementLocation getLocationForNodeType(final int locationNodeType, final IJavaElement javaElement) {
        switch (locationNodeType) {
        case ASTNode.BLOCK:
            return ElementLocation.BLOCK;
        case ASTNode.METHOD_DECLARATION:
            return ElementLocation.METHOD_DECLARATION;
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
            return ElementLocation.METHOD_DECLARATION_PARAMETER;
        case ASTNode.FIELD_DECLARATION:
            return ElementLocation.FIELD_DECLARATION;
        case ASTNode.TYPE_DECLARATION:
            return getTypeDeclarationLocation(javaElement);
        case ASTNode.IMPORT_DECLARATION:
            return ElementLocation.IMPORT_DECLARATION;
        case ASTNode.PACKAGE_DECLARATION:
            return ElementLocation.PACKAGE_DECLARATION;
        default:
            throw new IllegalStateException("Could not find location for " + locationNodeType);
        }
    }

    /**
     * @param javaElement
     *            The Java element occurring in the type declaration.
     * @return The {@link ElementLocation} - the "new" type, part of "extends"
     *         or part of "implements" - as identified from the Java element
     *         type.
     */
    private static ElementLocation getTypeDeclarationLocation(final IJavaElement javaElement) {
        if (javaElement instanceof SourceType) {
            return ElementLocation.TYPE_DECLARATION;
        } else {
            try {
                if (!((BinaryType) javaElement).isInterface()) {
                    return ElementLocation.TYPE_DECLARATION_EXTENDS;
                }
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return ElementLocation.TYPE_DECLARATION_IMPLEMENTS;
    }
}
