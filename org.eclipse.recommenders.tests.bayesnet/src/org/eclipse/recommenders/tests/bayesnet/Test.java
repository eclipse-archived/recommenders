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
package org.eclipse.recommenders.tests.bayesnet;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.bayesnet.util.SmileImporter;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;

import smile.Network;

public class Test {

    @org.junit.Test
    public void test() throws IOException {
        loadAndSave("example");
        loadAndSave("Lorg.eclipse.swt.widgets.Button");
    }

    private void loadAndSave(final String filename) throws IOException {
        final Network smileNetwork = new Network();
        smileNetwork.readFile(new File(filename + ".xdsl").getAbsolutePath());
        final SmileImporter importer = new SmileImporter(smileNetwork);
        final BayesianNetwork network = importer.getNetwork();

        GsonUtil.serialize(network, new File(filename + ".json"));

        final BayesianNetwork deserializedNetwork = GsonUtil.deserialize(new File("example.json"),
                BayesianNetwork.class);
        deserializedNetwork.restore();
        Assert.assertEquals(network.toString(), deserializedNetwork.toString());
    }
}
