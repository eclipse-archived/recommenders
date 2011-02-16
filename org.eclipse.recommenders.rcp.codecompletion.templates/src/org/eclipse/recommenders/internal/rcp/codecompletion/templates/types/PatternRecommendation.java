/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.types;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;

/**
 * Encapsulates one recommendation received from the models store.
 */
public final class PatternRecommendation implements Comparable<PatternRecommendation> {

    private String name;
    private ImmutableList<IMethodName> methods;
    private int probability;

    /**
     * @param name
     *            The name this pattern was given within the models store.
     * @param methods
     *            The pattern's methods as obtained from the model store.
     * @param probability
     *            Probability that this pattern is used in the observed
     *            occasion.
     * @return The <code>PatternRecommendation</code> encapsulating the given
     *         parameters.
     */
    public static PatternRecommendation create(final String name, final List<IMethodName> methods, final int probability) {
        final PatternRecommendation recommendation = new PatternRecommendation();
        recommendation.name = Checks.ensureIsNotNull(name);
        recommendation.methods = ImmutableList.copyOf(methods);
        recommendation.probability = probability;
        return recommendation;
    }

    /**
     * @return The name this pattern was given within the models store.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The pattern's methods as obtained from the model store.
     */
    public ImmutableList<IMethodName> getMethods() {
        return methods;
    }

    /**
     * @return Probability that this pattern is used in the observed occasion.
     */
    public int getProbability() {
        return probability;
    }

    @Override
    public int compareTo(final PatternRecommendation other) {
        return probability < other.probability ? -1 : (probability > other.probability ? 1 : 0);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
