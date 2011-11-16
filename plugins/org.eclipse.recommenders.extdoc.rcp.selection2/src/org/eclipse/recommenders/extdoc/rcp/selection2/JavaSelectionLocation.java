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
package org.eclipse.recommenders.extdoc.rcp.selection2;

public enum JavaSelectionLocation {

    WORKSPACE,
    PROJECT,

    CLASSPATH_CONTAINER,

    PACKAGE_FRAGMENT_ROOT,

    PACKAGE_DECLARATION,

    IMPORT_DECLARATION,

    TYPE_DECLARATION,
    TYPE_DECLARATION_EXTENDS,
    TYPE_DECLARATION_IMPLEMENTS,
    TYPE_BODY,

    FIELD_DECLARATION,
    FIELD_DECLARATION_INITIALIZER,

    METHOD_DECLARATION,
    METHOD_DECLARATION_RETURN,
    METHOD_DECLARATION_PARAMETER,
    METHOD_DECLARATION_THROWS,
    METHOD_BODY,
    //
    UNKNOWN,

}