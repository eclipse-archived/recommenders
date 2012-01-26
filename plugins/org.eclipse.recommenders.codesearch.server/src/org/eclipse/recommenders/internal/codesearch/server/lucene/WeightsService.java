/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.codesearch.server.lucene;

import java.util.Map;

import com.google.common.collect.Maps;

public class WeightsService {

    private Map<String, Float> weights = Maps.newLinkedHashMap();

    public float getWeight(final String featureName, final float defaultWeight) {
        final Float weight = weights.get(featureName);
        if (weight == null) {
            return defaultWeight;
        }
        return weight;
    }

    public Map<String, Float> getWeights() {
        return weights;
    }

    public Float getWeigth(final String scorerIdentifier) {
        return getWeight(scorerIdentifier, 0.0f);
    }

    public void setWeights(final Map<String, Float> newWeights) {
        weights = newWeights;
    }
}
