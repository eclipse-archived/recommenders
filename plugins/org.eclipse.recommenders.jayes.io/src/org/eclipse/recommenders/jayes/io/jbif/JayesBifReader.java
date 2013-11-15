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
package org.eclipse.recommenders.jayes.io.jbif;

import static org.eclipse.recommenders.jayes.io.jbif.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.io.IBayesNetReader;

import com.google.common.base.Charsets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

/**
 * Reader for the Jayes Binary Interchange Format (JBIF) written by {@link JayesBifWriter}.
 */
public class JayesBifReader implements IBayesNetReader {

    private InputStream in;

    public JayesBifReader(InputStream str) {
        in = str;
    }

    @Override
    public BayesNet read() throws IOException {
        return read(IOUtils.toByteArray(in));
    }

    private BayesNet read(byte[] array) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        try {
            return readBayesNet(buffer);
        } catch (RuntimeException e) {
            throw new IOException("Malformed data", e);
        }
    }

    private BayesNet readBayesNet(ByteBuffer buffer) throws IOException {
        BayesNet bayesNet = new BayesNet();

        readHeader(buffer);

        bayesNet.setName(readName(buffer));

        int nrNodes = buffer.getInt();

        for (int i = 0; i < nrNodes; i++) {
            readNodeDeclaration(bayesNet, buffer);
        }

        for (int i = 0; i < nrNodes; i++) {
            readNodeDefinition(bayesNet, bayesNet.getNode(i), buffer);
        }

        return bayesNet;
    }

    private void readHeader(ByteBuffer buffer) throws IOException {
        int magicNumber = buffer.getInt();
        if (magicNumber != MAGIC_NUMBER) {
            throw new IOException("Wrong magic number: " + Integer.toHexString(magicNumber).toUpperCase());
        }

        int formatVersion = buffer.getInt();
        if (formatVersion != FORMAT_VERSION) {
            throw new IOException("Wrong JBIF format version: " + formatVersion);
        }
    }

    private String readName(ByteBuffer buffer) {
        int byteCount = buffer.getShort() & 0xFFFF;

        byte[] bytes = new byte[byteCount];
        buffer.get(bytes);
        return new String(bytes, Charsets.UTF_8);
    }

    private void readNodeDeclaration(BayesNet bayesNet, ByteBuffer buffer) {
        BayesNode node = bayesNet.createNode(readName(buffer));

        int outcomeCount = buffer.getInt();

        String[] outcomes = new String[outcomeCount];
        for (int i = 0; i < outcomeCount; i++) {
            outcomes[i] = readName(buffer);
        }
        node.addOutcomes(outcomes);
    }

    private void readNodeDefinition(BayesNet bayesNet, BayesNode node, ByteBuffer buffer) throws IOException {
        node.setParents(readParents(bayesNet, buffer));

        node.setProbabilities(readCpt(buffer));
    }

    private List<BayesNode> readParents(BayesNet bayesNet, ByteBuffer buffer) throws IOException {
        int parentCount = buffer.get() & 0xFF;

        int[] parentIds = new int[parentCount];
        buffer.asIntBuffer().get(parentIds);
        buffer.position(buffer.position() + parentIds.length * Ints.BYTES);

        List<BayesNode> parents = new ArrayList<BayesNode>(parentCount);
        for (int parentId : parentIds) {
            parents.add(bayesNet.getNode(parentId));
        }

        return parents;
    }

    private double[] readCpt(ByteBuffer buffer) throws IOException {
        int entryCount = buffer.getInt();

        double[] probabilities = new double[entryCount];
        buffer.asDoubleBuffer().get(probabilities);
        buffer.position(buffer.position() + probabilities.length * Doubles.BYTES);

        return probabilities;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
