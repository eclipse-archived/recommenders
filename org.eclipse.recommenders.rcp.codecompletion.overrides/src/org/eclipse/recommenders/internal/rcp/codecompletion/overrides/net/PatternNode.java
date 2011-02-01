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
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.commons.utils.Throws.throwIllegalArgumentException;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import smile.Network;

public class PatternNode extends AbstractNode {
    static final String ID = "n0";

    protected PatternNode(final Network network) {
        super(network, ID);
    }

    public String[] getPatternNames() {
        return network.getOutcomeIds(getNodeId());
    }

    public void setEvidence(final String patternName) {
        assertPatternNameExists(patternName);
        network.setEvidence(getNodeId(), patternName);
    }

    private void assertPatternNameExists(final String patternName) {
        final String[] names = getPatternNames();
        final int indexOf = ArrayUtils.indexOf(names, patternName);
        if (indexOf == -1) {
            throwIllegalArgumentException("patternName does not exist in this network." + Arrays.toString(names));
        }
    }

    public String getEvidence() {
        ensureIsTrue(isEvidence(), "can get evidence if no evidence has been set before!");
        final int outcomeId = network.getEvidence(getNodeId());
        return getPatternNames()[outcomeId];
    }

    public double getProbability(final String patternName) {
        final String[] patternNames = getPatternNames();
        final int outcomeIndex = ArrayUtils.indexOf(patternNames, patternName);
        return network.getNodeValue(getNodeId())[outcomeIndex];
    }
}
