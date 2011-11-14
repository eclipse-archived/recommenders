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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Contains all required information about the user's selection of a java element in the perspective (e.g. Editor,
 * Package Explorer, Outline, ...).
 */
// TODO: implement - remove abstract afterwards and make final
public abstract class JavaSelection {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface JavaSelectionListener {

        JavaSelectionLocation[] value() default {};

    }

    abstract IJavaElement getElement();

    abstract JavaSelectionLocation getLocation();

    // AST related methods:

    abstract boolean hasAst();

    abstract AST getAst();

    abstract ASTNode getSelectedNode();

    // REMINDER: needed
    @Override
    abstract public int hashCode();

    @Override
    abstract public boolean equals(Object obj);
}
