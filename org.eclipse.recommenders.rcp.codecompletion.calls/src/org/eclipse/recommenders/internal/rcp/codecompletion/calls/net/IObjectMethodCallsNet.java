/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.net;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public interface IObjectMethodCallsNet {

    public abstract ITypeName getType();

    public abstract void setCalled(final IMethodName calledMethod);

    public abstract void updateBeliefs();

    public abstract void clearEvidence();

    public abstract void setMethodContext(final IMethodName newActiveMethodContext);

    public abstract void setObservedMethodCalls(final @Nullable ITypeName rebaseType,
            final Set<IMethodName> invokedMethods);

    public abstract SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold);

    public abstract SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(
            final double minProbabilityThreshold, final int maxNumberOfRecommendations);

    public abstract void negateConstructors();

    public abstract List<Tuple<String, Double>> getPatternsWithProbability();

    public abstract void setPattern(String patternName);

}