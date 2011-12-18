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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.base.Optional;

/**
 * Contains all required information about the user's selection of a java element in the perspective (e.g. Editor,
 * Package Explorer, Outline, ...).
 */
public class JavaSelection {

    private final IJavaElement element;
    private final JavaSelectionLocation location;
    private final ASTNode selection;

    public JavaSelection(final IJavaElement element, final JavaSelectionLocation location) {
        this(element, location, null);
    }

    public JavaSelection(final IJavaElement element, final JavaSelectionLocation location, final ASTNode selection) {
        this.element = element;
        this.location = location;
        this.selection = selection;
    }

    public IJavaElement getElement() {
        return element;
    }

    public JavaSelectionLocation getLocation() {
        return location;
    }

    public boolean hasSelectedNode() {
        return selection != null;
    }

    // review: should we throw an exception if selection ==null instead of using Optional. There is a hasSelectedNode
    // method that should be called before.
    public Optional<ASTNode> getSelectedNode() {
        return fromNullable(selection);
    }

    @Override
    public boolean equals(final Object obj) {
        return reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface JavaSelectionListener {

        JavaSelectionLocation[] value() default {};

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
