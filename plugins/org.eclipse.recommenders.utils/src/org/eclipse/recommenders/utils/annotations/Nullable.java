/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated variable may be / is allowed to be
 * <code>null</code>. Programmers using such variables need to ensure
 * appropriate handling, clients passing arguments to method annotated with this
 * annotation may pass null for the annotated arguments.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.FIELD, ElementType.METHOD })
@Inherited
public @interface Nullable {
    String value() default "";
}
