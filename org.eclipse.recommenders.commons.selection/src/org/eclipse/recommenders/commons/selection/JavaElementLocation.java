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

/**
 * A Java element's location in the code.
 */
public enum JavaElementLocation {

    PACKAGE_DECLARATION, IMPORT_DECLARATION, TYPE_DECLARATION, TYPE_DECLARATION_EXTENDS, TYPE_DECLARATION_IMPLEMENTS, FIELD_DECLARATION, METHOD_DECLARATION, METHOD_DECLARATION_PARAMETER, BLOCK;

    public static boolean isInTypeDeclaration(final JavaElementLocation location) {
        return location == TYPE_DECLARATION || location == TYPE_DECLARATION_EXTENDS
                || location == TYPE_DECLARATION_IMPLEMENTS;
    }
}