/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.statics;

import java.util.List;

import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.annotations.Beta;

/**
 * A thin layer around a Bayesian network designed for recommending static method calls.
 */
@Beta
public interface IStaticsModel {

    /**
     * Returns the type this net makes recommendations for.
     */
    ITypeName getDeclaringType();

    /**
     * Clears all observations and puts the network in its initial state.
     */
    void reset();

    /**
     * Sets the (name of) the enclosing method.
     *
     * @return returns true if this method name context was known in this model and could be set.
     */
    boolean setEnclosingMethod(IMethodName context);

    /**
     * Returns a list of recommended static method calls.
     */
    List<Recommendation<IMethodName>> recommendCalls();

    double recommendCall(IMethodName method);

}
