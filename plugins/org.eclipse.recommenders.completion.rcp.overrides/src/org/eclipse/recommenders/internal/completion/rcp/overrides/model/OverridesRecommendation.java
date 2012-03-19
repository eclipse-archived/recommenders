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
package org.eclipse.recommenders.internal.completion.rcp.overrides.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.rcp.IRecommendation;
import org.eclipse.recommenders.utils.names.IMethodName;

public class OverridesRecommendation implements IRecommendation {
    public final IMethodName method;

    public final double probability;

    public OverridesRecommendation(final IMethodName method, final double probability) {
        this.method = method;
        this.probability = probability;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    @Override
    public double getProbability() {
        return probability;
    }
}
