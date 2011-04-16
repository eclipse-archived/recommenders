/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayes;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.utils.Tuple;

import smile.Network;
import smile.SMILEException;

import com.google.common.collect.Lists;

public class NodeWrapper {

    private final Node node;
    private final Network smileNetwork;
    private int nodeHandle;
    private HashMap<String, Integer> stateMapping;

    public NodeWrapper(final Node node, final Network smileNetwork) {
        this.node = node;
        this.smileNetwork = smileNetwork;
        initialize();
    }

    private void initialize() {
        nodeHandle = smileNetwork.addNode(Network.NodeType.Cpt);

        final String[] states = node.getStates();
        stateMapping = new HashMap<String, Integer>();
        for (int i = 0; i < states.length; i++) {
            stateMapping.put(states[i], i);
            if (i > 1) {
                smileNetwork.addOutcome(nodeHandle, "state_" + String.valueOf(i));
            }
        }
    }

    public String[] getStates() {
        return node.getStates();
    }

    public int getHandle() {
        return nodeHandle;
    }

    public void observeState(final String state) {
        if (stateMapping.containsKey(state)) {
            smileNetwork.setEvidence(nodeHandle, stateMapping.get(state));
        }
    }

    public boolean isEvidence() {
        return smileNetwork.isEvidence(nodeHandle);
    }

    public int getStateIndex(final String state) {
        return stateMapping.get(state);
    }

    public double[] getProbability() {
        try {
            return smileNetwork.getNodeValue(nodeHandle);
        } catch (final SMILEException x) {
            final File tmpDir = SystemUtils.getJavaIoTmpDir();
            final File netFile = new File(tmpDir, "error.xdsl");
            final File logFile = new File(tmpDir, "error.log");
            try {
                FileUtils.write(logFile, x.toString());
                smileNetwork.writeFile(netFile.getAbsolutePath());
            } catch (final Exception e) {
                // silently ignore if writing debug output fails
            }
            return new double[stateMapping.size()];
        }
    }

    public List<Tuple<String, Double>> getStatesWithProbability() {
        final double[] probs = smileNetwork.getNodeValue(nodeHandle);
        final List<Tuple<String, Double>> res = Lists.newArrayListWithCapacity(probs.length);
        final String[] states = node.getStates();
        for (int i = 0; i < states.length; i++) {
            final int probIndex = stateMapping.get(states[i]);
            final double p = probs[probIndex];
            if (0.01 > p) {
                continue;
            }
            final String name = states[i];
            res.add(Tuple.create(name, p));
        }
        return res;
    }

}
