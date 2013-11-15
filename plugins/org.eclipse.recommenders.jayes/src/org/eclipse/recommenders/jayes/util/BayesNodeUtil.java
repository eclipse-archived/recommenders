/*******************************************************************************
 * Copyright (c) 2013 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.jayes.util;

import java.util.Map;

import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.factor.AbstractFactor;
import org.eclipse.recommenders.jayes.factor.Cut;

public class BayesNodeUtil {

    /**
     * 
     * @param evidence
     *            an evidence map containing mappings for all of the node's parents
     * @return the distribution P(node|evidence)
     */
    public static double[] getSubCpt(BayesNode node, Map<BayesNode, String> evidence) {
        AbstractFactor factor = node.getFactor();
        for (final BayesNode p : node.getParents()) {
            if (evidence.containsKey(p)) {
                factor.select(p.getId(), p.getOutcomeIndex(evidence.get(p)));
            } else {
                throw new IllegalArgumentException("evidence does not cover all parents of node");
            }
        }

        Cut cut = new Cut(factor);
        cut.initialize();

        if (cut.getSubCut() != null || cut.getStepSize() != 1
                || (cut.getEnd() - cut.getStart()) != node.getOutcomeCount()) {
            throw new AssertionError("Unexpected factor structure: node's dimension is not the lowest");
        }

        double[] subCpt = new double[node.getOutcomeCount()];

        for (int i = 0; i < node.getOutcomeCount(); i++) {
            subCpt[i] = factor.getValue(cut.getStart() + i * cut.getStepSize());
        }

        factor.resetSelections();

        return subCpt;
    }

}
