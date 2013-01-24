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
package org.eclipse.recommenders.internal.completion.rcp.calls.net;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite;
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.utils.codestructs.ObjectUsage;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

public interface IObjectMethodCallsNet {

    public abstract ITypeName getType();

    public abstract void setCalled(final IMethodName calledMethod);

    public abstract void clearEvidence();

    public abstract void setMethodContext(final IMethodName newActiveMethodContext);

    public abstract void setKind(final DefinitionSite.Kind newKind);

    public abstract void setDefinition(final IMethodName newDefinition);

    public abstract void setObservedMethodCalls(final @Nullable ITypeName rebaseType,
            final Set<IMethodName> invokedMethods);

    public abstract void setQuery(final ObjectUsage query);

    public abstract SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold);

    public abstract SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(
            final double minProbabilityThreshold, final int maxNumberOfRecommendations);

    public abstract List<Tuple<String, Double>> getPatternsWithProbability();

    public abstract void setPattern(final String patternName);

    public abstract Collection<IMethodName> getMethodCalls();

    public abstract Collection<IMethodName> getContexts();

    public abstract IMethodName getActiveContext();

    public abstract IMethodName getActiveDefinition();

    public abstract Kind getActiveKind();

    public abstract Set<IMethodName> getActiveCalls();

    public abstract Set<Tuple<String, Double>> getDefinitions();
}
