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
package org.eclipse.recommenders.rcp.events;

import static com.google.common.base.Optional.fromNullable;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.base.Optional;

/**
 * Contains all required information about the user's selection of a java element in the perspective (e.g. Editor,
 * Package Explorer, Outline, ...).
 */
public class JavaSelectionEvent {

    private final IJavaElement element;
    private final JavaSelectionLocation location;
    private final Optional<ASTNode> selection;

    public JavaSelectionEvent(final IJavaElement element, final JavaSelectionLocation location) {
        this(element, location, null);
    }

    public JavaSelectionEvent(final IJavaElement element, final JavaSelectionLocation location, final ASTNode selection) {
        this.element = element;
        this.location = location;
        this.selection = fromNullable(selection);
    }

    public IJavaElement getElement() {
        return element;
    }

    public JavaSelectionLocation getLocation() {
        return location;
    }

    public Optional<ASTNode> getSelectedNode() {
        return selection;
    }

    @Override
    public boolean equals(final Object obj) {
        return reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    public static enum JavaSelectionLocation {

        WORKSPACE, PROJECT,

        CLASSPATH_CONTAINER,

        PACKAGE_FRAGMENT_ROOT,

        PACKAGE_DECLARATION,

        IMPORT_DECLARATION,

        TYPE_DECLARATION, TYPE_DECLARATION_EXTENDS, TYPE_DECLARATION_IMPLEMENTS,
        // TYPE_BODY,

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
}
