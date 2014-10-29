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
package org.eclipse.recommenders.jayes.sampling;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.testing.jayes.NetExamples;
import org.junit.Test;

public class BasicSamplerTest {

    @Test
    public void testSamplesCoverAllVariables() {
        BasicSampler sampler = new BasicSampler();
        BayesNet net = NetExamples.testNet1();
        sampler.setNetwork(net);

        assertThat(sampler.sample().keySet(), hasItems(net.getNodes().toArray(new BayesNode[0])));
    }

    @Test
    public void testUnconnectedNetwork() {
        BasicSampler sampler = new BasicSampler();
        BayesNet net = NetExamples.unconnectedNet();
        sampler.setNetwork(net);

        assertThat(sampler.sample().keySet(), hasItems(net.getNodes().toArray(new BayesNode[0])));
    }

}
