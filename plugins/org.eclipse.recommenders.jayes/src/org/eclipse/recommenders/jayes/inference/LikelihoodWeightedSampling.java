/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.jayes.inference;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.sampling.BasicSampler;
import org.eclipse.recommenders.jayes.util.BayesUtils;
import org.eclipse.recommenders.jayes.util.MathUtils;

public class LikelihoodWeightedSampling extends AbstractInferer {

    private int sampleCount = 200;
    private final BasicSampler sampler = new BasicSampler();

    @Override
    public void setNetwork(final BayesNet bn) {
        super.setNetwork(bn);
        sampler.setBN(bn);

    }

    @Override
    protected void updateBeliefs() {
        sampler.setEvidence(evidence);
        for (int i = 0; i < sampleCount; i++) {
            final Map<BayesNode, String> sample = sampler.sample();
            final double weight = computeEvidenceProbability(sample);

            for (final Entry<Integer, Integer> e : BayesUtils.toIntegerMap(sample).entrySet()) {
                beliefs[e.getKey()][e.getValue()] += weight;
            }
        }

        normalizeBeliefs();

    }

    private void normalizeBeliefs() {
        for (int i = 0; i < beliefs.length; i++) {
            beliefs[i] = MathUtils.normalize(beliefs[i]);
        }
    }

    private double computeEvidenceProbability(final Map<BayesNode, String> sample) {
        double factor = 1.0;
        for (final BayesNode n : net.getNodes()) {
            // REVIEW: XXX integer is incompatible to evicende.BayesNode. This never returns true!
            final int id = n.getId();
            if (evidence.containsKey(id)) {
                factor *= n.marginalize(sample)[n.getOutcomeIndex(evidence.get(n))];
            }
        }
        return factor;
    }

    public void setSampleCount(final int sampleCount) {
        this.sampleCount = sampleCount;
    }

}
