/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.codesearch;

import java.util.Map;

public class FeatureWeights {

    public Map<String, Float> weights;

    public float getWeight(final String featureId) {
        return weights.containsKey(featureId) ? weights.get(featureId) : 0.0f;
    }
}
