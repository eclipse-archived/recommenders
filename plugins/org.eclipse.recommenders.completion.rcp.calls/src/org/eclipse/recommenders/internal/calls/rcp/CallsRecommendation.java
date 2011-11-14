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
package org.eclipse.recommenders.internal.calls.rcp;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.IRecommendation;
import org.eclipse.recommenders.utils.names.IMethodName;

public class CallsRecommendation implements IRecommendation, Comparable<CallsRecommendation> {
    public Variable local;

    public IMethodName method;

    public double probability;

    public static CallsRecommendation create(final Variable local, final IMethodName recommendedMethodCall,
            final double methodCallProbability) {
        final CallsRecommendation res = new CallsRecommendation();
        res.local = local;
        res.method = recommendedMethodCall;
        res.probability = methodCallProbability;
        return res;
    }

    /**
     * Returns the recommendation with the higher probability first. If both
     * compilationUnits2recommendationsIndex have the same probability it
     * returns the recommendation with the lexigraphically lower method name.
     */
    @Override
    public int compareTo(final CallsRecommendation other) {
        final int probabilityCmp = Double.compare(other.probability, probability);
        return probabilityCmp != 0 ? probabilityCmp : method.compareTo(other.method);
    }

    @Override
    public double getProbability() {
        return probability;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
