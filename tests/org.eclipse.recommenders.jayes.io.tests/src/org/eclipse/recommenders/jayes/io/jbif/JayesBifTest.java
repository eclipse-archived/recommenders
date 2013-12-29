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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.recommenders.jayes.BayesNet;
import org.junit.Test;

public class JayesBifTest {

    @Test
    public void testReadDefaultNode() throws IOException {
        // create simple network
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // header
        buffer.putInt(Constants.MAGIC_NUMBER);
        buffer.putInt(Constants.FORMAT_VERSION);
        // network
        buffer.putShort((short) 0);
        buffer.putInt(1);
        // node declaration
        buffer.putShort((short) 0);
        buffer.putInt(2);
        buffer.putShort((short) 1);
        buffer.put((byte) 'a');
        buffer.putShort((short) 1);
        buffer.put((byte) 'b');
        // node definition
        buffer.put((byte) 0);
        buffer.putInt(2);
        buffer.putDouble(0.4);
        buffer.putDouble(0.6);

        byte[] array = buffer.array();

        ByteArrayInputStream in = new ByteArrayInputStream(array);

        JayesBifReader reader = new JayesBifReader(in);
        BayesNet net = reader.read();
        reader.close();

        assertThat(net.getNodes().size(), is(1));
        assertThat(net.getNode("").getOutcomeCount(), is(2));
        assertThat(net.getNode("").getOutcomeName(0), is("a"));
        assertThat(net.getNode("").getOutcomeName(1), is("b"));
        assertThat(net.getNode("").getProbabilities(), is(new double[] { 0.4, 0.6 }));

    }

}
