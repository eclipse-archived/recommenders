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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.recommenders.commons.selection.ElementLocation;

public class ElementLocationResolver {

    public ElementLocation resolve(final ASTNode astNode) {
        if (astNode == null) {
            return null;
        }

        ASTNode node = astNode;
        int nodeType = node.getNodeType();
        while (nodeType == ASTNode.SIMPLE_NAME || nodeType == ASTNode.SIMPLE_TYPE
                || nodeType == ASTNode.SINGLE_VARIABLE_DECLARATION || nodeType == ASTNode.QUALIFIED_NAME
                || nodeType == ASTNode.METHOD_INVOCATION || nodeType == ASTNode.EXPRESSION_STATEMENT
                || nodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT
                || nodeType == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
            node = node.getParent();
            nodeType = node.getNodeType();
        }
        return getLocationForNodeType(node);
    }

    private ElementLocation getLocationForNodeType(final ASTNode node) {
        final int nodeType = node.getNodeType();

        switch (nodeType) {
        case ASTNode.BLOCK:
            return ElementLocation.BLOCK;
        case ASTNode.METHOD_DECLARATION:
            return ElementLocation.METHOD_DECLARATION;
        case ASTNode.FIELD_DECLARATION:
            return ElementLocation.FIELD_DECLARATION;
        case ASTNode.TYPE_DECLARATION:
            return ElementLocation.TYPE_DECLARATION;
        case ASTNode.IMPORT_DECLARATION:
            return ElementLocation.IMPORT_DECLARATION;
        case ASTNode.PACKAGE_DECLARATION:
            return ElementLocation.PACKAGE_DECLARATION;
        default:
            throw new IllegalStateException("Could not find location for " + nodeType);
        }
    }

}
