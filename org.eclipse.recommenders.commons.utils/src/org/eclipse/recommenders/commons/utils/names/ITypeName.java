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
package org.eclipse.recommenders.commons.utils.names;

/**
 * Represents the name for java types. Java types follow a syntax that contain
 * interesting information like it the given type name an array type, a
 * primitive etc. This information is made available here.
 * <p>
 */
public interface ITypeName extends IName, Comparable<ITypeName> {

    /**
     * Returns the base-type of this array-type. Note, this requires
     * {@link #isArrayType()} to return {@code true}.
     */
    public abstract ITypeName getArrayBaseType();

    /**
     * Returns the {@link IMethodName} within this type was declared.
     * <p>
     * Note, this is only valid for nested classes and might throw an exception
     * if this type was not declared within a method.
     */
    public abstract IMethodName getDeclaringMethod();

    /**
     * Returns the {@link ITypeName} within this type was declared.
     * <p>
     * Note, this is only valid for nested classes and might throw an exception
     * if this type was not declared directly within a type.
     */
    public abstract ITypeName getDeclaringType();

    /**
     * Returns the package of this {@code ITypeName}. This method always returns
     * a non-null object. In the case of primitive or array types a package name
     * representing the default package is returned.
     */
    public abstract IPackageName getPackage();

    /**
     * Returns the name of the class without its package name.
     */
    public abstract String getClassName();

    /**
     * Returns {@code true} if this type is an anonymous type.
     */
    public abstract boolean isAnonymousType();

    /**
     * Returns {@code true} if this type is an array type
     */
    public abstract boolean isArrayType();

    /**
     * Returns {@code true} if this type is an declared type like a class or
     * interface.
     */
    public abstract boolean isDeclaredType();

    /**
     * Returns {@code true} if this type is a nested/inner type.
     */
    public abstract boolean isNestedType();

    /**
     * Returns {@code true} if this type is one of java's built-in primitive
     * types.
     */
    public abstract boolean isPrimitiveType();

    public abstract boolean isVoid();

    /**
     * Returns the fully qualified name of this type. The concrete string
     * representation depends on the implementor. However, we recommend to use
     * the vm style representation java types.
     */
    @Override
    public abstract String toString();

    /**
     * Returns the number of array dimensions for the given type name. This
     * method returns '0' for normal types and '1' or more for array types.
     */
    int getArrayDimensions();
}
