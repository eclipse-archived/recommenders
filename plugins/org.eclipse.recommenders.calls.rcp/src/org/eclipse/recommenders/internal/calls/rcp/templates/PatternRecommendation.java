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

import java.util.Objects;

import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableSet;

/**
 * Encapsulates one recommendation received from the model.
 */
public class PatternRecommendation extends Recommendation<ImmutableSet<IMethodName>> {

    private String name;
    private ITypeName type;

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
    public PatternRecommendation(String name, ITypeName type, ImmutableSet<IMethodName> methods, double probability) {
        super(methods, probability);
        this.name = Checks.ensureIsNotNull(name);
        this.type = Checks.ensureIsNotNull(type);
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

    @Override
    public int hashCode() {
        return Objects.hash(type, getProposal());
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof PatternRecommendation
                && getProposal().equals(((PatternRecommendation) object).getProposal());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this).add("calls", getProposal()).add("prob", getRelevance()).add("type", type) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .add("name", name).toString(); //$NON-NLS-1$
    }
}
