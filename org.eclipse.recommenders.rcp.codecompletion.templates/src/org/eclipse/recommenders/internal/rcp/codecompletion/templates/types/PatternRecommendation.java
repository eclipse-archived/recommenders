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

import java.util.Set;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;

/**
 * Encapsulates one recommendation received from the models store.
 */
public final class PatternRecommendation implements Comparable<PatternRecommendation> {

    private String name;
    private ImmutableCollection<IMethodName> methods;
    private int probability;

    public static PatternRecommendation create(final String name, final Set<IMethodName> methods, final int probability) {
        final PatternRecommendation recommendation = new PatternRecommendation();
        recommendation.name = Checks.ensureIsNotNull(name);
        recommendation.methods = ImmutableSet.copyOf(methods);
        recommendation.probability = probability;
        return recommendation;
    }

    public String getName() {
        return name;
    }

    public ImmutableCollection<IMethodName> getMethods() {
        return methods;
    }

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
