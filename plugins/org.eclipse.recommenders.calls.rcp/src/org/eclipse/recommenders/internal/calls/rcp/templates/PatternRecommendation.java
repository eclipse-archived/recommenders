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
package org.eclipse.recommenders.internal.calls.rcp.templates;

import java.util.Collection;

import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableList;

/**
 * Encapsulates one recommendation received from the models store.
 */
public final class PatternRecommendation implements Comparable<PatternRecommendation> {

    private final String name;
    private final ITypeName type;
    private final ImmutableList<IMethodName> methods;
    private final double probability;

    /**
     * @param name
     *            The name this pattern was given within the models store.
     * @param type
     *            The type of the variable this recommendation was created for.
     * @param methods
     *            The pattern's methods as obtained from the model store.
     * @param probability
     *            Probability that this pattern is used in the observed occasion.
     */
    public PatternRecommendation(final String name, final ITypeName type, final Collection<IMethodName> methods,
            final double probability) {
        this.name = Checks.ensureIsNotNull(name);
        this.type = Checks.ensureIsNotNull(type);
        this.methods = ImmutableList.copyOf(methods);
        this.probability = probability;
    }

    /**
     * @return The name this pattern was given within the models store.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The type of the variable this recommendation was created for.
     */
    public ITypeName getType() {
        return type;
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
    public double getProbability() {
        return probability;
    }

    @Override
    public int hashCode() {
        return methods.hashCode();
    }

    @Override
    public int compareTo(final PatternRecommendation other) {
        return Double.valueOf(probability).compareTo(other.probability);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof PatternRecommendation && methods.equals(((PatternRecommendation) object).methods);
    }
}
