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

import org.apache.commons.lang3.text.WordUtils;

/**
 * A Java element's location in the code.
 */
public enum JavaElementLocation {

    PACKAGE_DECLARATION, IMPORT_DECLARATION, TYPE_DECLARATION, EXTENDS_DECLARATION, IMPLEMENTS_DECLARATION, FIELD_DECLARATION, METHOD_DECLARATION, PARAMETER_DECLARATION, METHOD_BODY;

    private String displayName;

    JavaElementLocation() {
        displayName = WordUtils.capitalizeFully(name().replace("_", " "));
    }

    public String getDisplayName() {
        return displayName;
    }

    // TODO: Only called from tests.
    public static boolean isInTypeDeclaration(final JavaElementLocation location) {
        return location == TYPE_DECLARATION || location == EXTENDS_DECLARATION || location == IMPLEMENTS_DECLARATION;
    }
}