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

import java.io.Serializable;

/**
 * A {@link IMethodName} is basically the full qualified method name. This class provides an easy way to access the
 * information available in such a method name (like isInit, isSyntetic etc.) and provides some safety checks for the
 * format of such a full qualified name.
 */
public interface IMethodName extends IName, Comparable<IMethodName>, Serializable {

    IMethodName[] EMPTY = {};

    /**
     * Returns the a string containing the argument and return types.
     */
    public abstract String getDescriptor();

    /**
     * Returns the name, i.e., the char sequence between the declaring type and the brackets. Example: Invoking
     * getName() on MethdName like "Ljava/lang/Object.equals(Ljava/lang/Object;)Z" will result in "equals".
     */
    public abstract String getName();

    /**
     * Returns the method name followed by its descriptor.
     * 
     * @see #getDescriptor()
     */
    public abstract String getSignature();

    /**
     * (Misleading name) Returns {@code true} iff this method name is identical to the given method but ignores the
     * declaring type. This kind of similarity is used to check whether one method may override another method (but note
     * this criterion is required but not sufficient to check the overrides relation between two methods!).
     */
    public abstract boolean similar(final IMethodName other);

    /**
     * Returns the {@link ITypeName} of the declaring class, i.e., the class that statically defines this method.
     */
    public abstract ITypeName getDeclaringType();

    /**
     * Returns the declared parameter types of this method.
     */
    public abstract ITypeName[] getParameterTypes();

    /**
     * Returns the declared return type of this method.
     */
    public abstract ITypeName getReturnType();

    /**
     * Returns {@code true} if the method's identifier equals <i>&lt;init&gt;</i>.
     */
    public abstract boolean isInit();

    /**
     * Returns {@code true} if the method's identifier equals <i>&lt;clinit&gt;</i>.
     */
    public abstract boolean isStaticInit();

    /**
     * Returns {@code true} iff this method is synthetic, i.e., it contains a $ sign in its identifier
     */
    public abstract boolean isSynthetic();

    /**
     * Returns the fully qualified name of this method which consists of
     * <ol>
     * <li>the name of the declaring class,
     * <li>the method's name, and
     * <li>the method's parameter types.
     * </ol>
     * The concrete string representation depends on the implementor. However, we recommend to use the vm style
     * representation java types.
     */
    @Override
    public abstract String toString();

    public abstract boolean isVoid();

    public abstract boolean hasParameters();
}
