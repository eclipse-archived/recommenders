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
package org.eclipse.recommenders.utils.names;

/**
 * Represents the name for java types. Java types follow a syntax that contain interesting information like it the given
 * type name an array type, a primitive etc. This information is made available here.
 * <p>
 */
public interface ITypeName extends IName, Comparable<ITypeName> {

    ITypeName[] EMPTY = {};

    /**
     * Returns the base-type of this array-type. Note, this requires {@link #isArrayType()} to return {@code true}.
     */
    ITypeName getArrayBaseType();

    /**
     * Returns the {@link IMethodName} within this type was declared.
     * <p>
     * Note, this is only valid for nested classes and might throw an exception if this type was not declared within a
     * method.
     */
    IMethodName getDeclaringMethod();

    /**
     * Returns the {@link ITypeName} within this type was declared.
     * <p>
     * Note, this is only valid for nested classes and might throw an exception if this type was not declared directly
     * within a type.
     */
    ITypeName getDeclaringType();

    /**
     * Returns the package of this {@code ITypeName}. This method always returns a non-null object. In the case of
     * primitive or array types a package name representing the default package is returned.
     */
    IPackageName getPackage();

    /**
     * Returns the name of the class without its package name.
     */
    String getClassName();

    /**
     * Returns {@code true} if this type is an anonymous type.
     */
    boolean isAnonymousType();

    /**
     * Returns {@code true} if this type is an array type
     */
    boolean isArrayType();

    /**
     * Returns {@code true} if this type is an declared type like a class or interface.
     */
    boolean isDeclaredType();

    /**
     * Returns {@code true} if this type is a nested/inner type.
     */
    boolean isNestedType();

    /**
     * Returns {@code true} if this type is one of java's built-in primitive types.
     */
    boolean isPrimitiveType();

    boolean isVoid();

    /**
     * Returns the fully qualified name of this type. The concrete string representation depends on the implementor.
     * However, we recommend to use the vm style representation java types.
     */
    @Override
    String toString();

    /**
     * Returns the number of array dimensions for the given type name. This method returns '0' for normal types and '1'
     * or more for array types.
     */
    int getArrayDimensions();
}
