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

    ITypeName getType();

    void setCalled(final IMethodName calledMethod);

    void clearEvidence();

    void setMethodContext(final IMethodName newActiveMethodContext);

    void setKind(final DefinitionSite.Kind newKind);

    void setDefinition(final IMethodName newDefinition);

    void setObservedMethodCalls(@Nullable final ITypeName rebaseType, final Set<IMethodName> invokedMethods);

    void setQuery(final ObjectUsage query);

    SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold);

    SortedSet<Tuple<IMethodName, Double>> getRecommendedMethodCalls(final double minProbabilityThreshold,
            final int maxNumberOfRecommendations);

    List<Tuple<String, Double>> getPatternsWithProbability();

    void setPattern(final String patternName);

    Collection<IMethodName> getMethodCalls();

    Collection<IMethodName> getContexts();

    IMethodName getActiveContext();

    IMethodName getActiveDefinition();

    Kind getActiveKind();

    Set<IMethodName> getActiveCalls();

    Set<Tuple<String, Double>> getDefinitions();
}
