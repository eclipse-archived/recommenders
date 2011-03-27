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
package org.eclipse.recommenders.commons.utils;

import static org.eclipse.recommenders.commons.utils.Fingerprints.sha1;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("unused")
public class FingerprintsTest {

    // expected value computed using http://www.sha1.cz/
    private static String[][] data = new String[][] { { "this is a simple test",
            "2acdddb97c144820e5741b4218fc77c2bca8efa5" }, };

    private static final File PATH_TO_RT_JAR = new File(SystemUtils.getJavaHome(), "/lib/rt.jar");

    @Test
    public void testSimpleSha1Message() {
        final String message = data[0][0];
        final String expectedSha1 = data[0][1];
        final String actualSha1 = sha1(message);
        assertEquals(expectedSha1, actualSha1);
    }

    @Test
    public void testSha1InputStream() {
        final byte[] input = data[0][0].getBytes();
        final String expectedSha1 = data[0][1];
        final ByteArrayInputStream stream = new ByteArrayInputStream(input);
        final String actualSha1 = sha1(stream);
        assertEquals(expectedSha1, actualSha1);
    }

    @Test(expected = Exception.class)
    @Ignore
    public void testSha1InputStream_ThrowingException() throws IOException {
        final InputStream stream = mock(InputStream.class);
        doThrow(new IOException("mock")).when(stream).read();
        sha1(stream);
    }

    @Test
    public void testSimpleSha1FromFile() throws IOException {
        final String message = data[0][0];
        final String expectedSha1 = data[0][1];
        final File file = createTempoaryFile(message);
        final String actualSha1 = sha1(file);
        assertEquals(expectedSha1, actualSha1);
    }

    @Test(timeout = 3000)
    public void testSha1PerformanceRTJarOnMarcelsMachine() {
        // setup:
        if (!PATH_TO_RT_JAR.exists()) {
            return;
        }
        // execute:
        final String sha1 = sha1(PATH_TO_RT_JAR);
        // System.out.println(sha1);
    }

    private File createTempoaryFile(final String message) throws IOException, FileNotFoundException {
        final File f = File.createTempFile("test.sha1", ".txt");
        f.deleteOnExit();
        final FileOutputStream fos = new FileOutputStream(f);
        fos.write(message.getBytes());
        fos.close();
        return f;
    }
}
