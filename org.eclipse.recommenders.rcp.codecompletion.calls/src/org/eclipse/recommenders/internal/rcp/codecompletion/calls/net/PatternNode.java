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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.net;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.commons.utils.Throws.throwIllegalArgumentException;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.recommenders.commons.utils.Tuple;

import smile.Network;

import com.google.common.collect.Lists;

public class PatternNode extends AbstractNode {
    private static final double MIN_PROBABILITY = 0.01d;
    static final String ID = "n0";

    protected PatternNode(final Network network) {
        super(network, ID);
    }

    public String[] getPatternNames() {
        return network.getOutcomeIds(getNodeId());
    }

    public void setPattern(final String patternName) {
        assertPatternNameExists(patternName);
        setEvidence(patternName);
    }

    private void assertPatternNameExists(final String patternName) {
        final String[] names = getPatternNames();
        final int indexOf = ArrayUtils.indexOf(names, patternName);
        if (indexOf == -1) {
            throwIllegalArgumentException("patternName does not exist in this network." + Arrays.toString(names));
        }
    }

    public String getPattern() {
        ensureIsTrue(isEvidence(), "can get evidence if no evidence has been set before!");
        final int outcomeId = network.getEvidence(getNodeId());
        return getPatternNames()[outcomeId];
    }

    public double getProbability(final String patternName) {
        final String[] patternNames = getPatternNames();
        final int outcomeIndex = ArrayUtils.indexOf(patternNames, patternName);
        final double p = getValues()[outcomeIndex];
        return p;

    }

    /**
     * Returns a list of all applicable patterns with their probability.
     * Patterns that cannot be observed in current state are skipped.
     * 
     * @return List&lt;Tuple&lt;pattern name, probability&gt;&gt;
     */
    public List<Tuple<String/* pattern name */, Double/* probability */>> getPatternsWithProbability() {
        final String[] names = getPatternNames();
        final double[] probs = network.getNodeValue(getNodeId());
        ensureIsTrue(names.length == probs.length);
        final List<Tuple<String, Double>> res = Lists.newArrayListWithCapacity(names.length);
        for (int i = 0; i < names.length; i++) {
            if (MIN_PROBABILITY > probs[i]) {
                continue;
            }
            final double p = probs[i];
            final String name = names[i];
            res.add(Tuple.create(name, p));
        }
        return res;
    }
}
