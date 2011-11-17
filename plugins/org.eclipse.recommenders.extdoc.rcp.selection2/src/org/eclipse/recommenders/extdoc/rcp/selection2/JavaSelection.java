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
 * Contains all required information about the user's selection of a java
 * element in the perspective (e.g. Editor, Package Explorer, Outline, ...).
 */
public class JavaSelection {

    private final IJavaElement element;
    private final JavaSelectionLocation location;
    private final ASTNode selection;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface JavaSelectionListener {

        JavaSelectionLocation[] value() default {};

    }

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
}
