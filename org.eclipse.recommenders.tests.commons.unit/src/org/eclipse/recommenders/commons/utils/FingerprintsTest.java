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

import static org.eclipse.recommenders.commons.utils.Fingerprints.internal_sha1v2;
import static org.eclipse.recommenders.commons.utils.Fingerprints.sha1;
import static org.eclipse.recommenders.commons.utils.Fingerprints.toHexString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

@SuppressWarnings("unused")
public class FingerprintsTest {

    // expected value computed using http://www.sha1.cz/
    private static String[][] data = new String[][] { { "this is a simple test",
            "2acdddb97c144820e5741b4218fc77c2bca8efa5" }, };

    private static final File PATH_TO_RT_JAR = new File(SystemUtils.getJavaHome(), "/lib/rt.jar");

    @Test
    public void testSimpleSha1Message() {
        String message = data[0][0];
        String expectedSha1 = data[0][1];
        String actualSha1 = toHexString(sha1(message));
        assertEquals(expectedSha1, actualSha1);
    }

    @Test
    public void testSha1InputStream() {
        byte[] input = data[0][0].getBytes();
        String expectedSha1 = data[0][1];
        ByteArrayInputStream stream = new ByteArrayInputStream(input);
        String actualSha1 = toHexString(sha1(stream));
        assertEquals(expectedSha1, actualSha1);
    }

    @Test(expected = Exception.class)
    public void testSha1InputStream_ThrowingException() throws IOException {
        InputStream stream = mock(InputStream.class);
        doThrow(new IOException("mock")).when(stream).read();
        sha1(stream);
    }

    @Test
    public void testSimpleSha1FromFile() throws IOException {
        String message = data[0][0];
        String expectedSha1 = data[0][1];
        File file = createTempoaryFile(message);
        String actualSha1 = toHexString(sha1(file));
        assertEquals(expectedSha1, actualSha1);
    }

    @Test(timeout = 3000)
    public void testSha1PerformanceRTJarOnMarcelsMachine() {
        // setup:
        if (!PATH_TO_RT_JAR.exists()) {
            return;
        }
        // execute:
        String sha1 = toHexString(sha1(PATH_TO_RT_JAR));
        // System.out.println(sha1);
    }

    @Test(timeout = 3000)
    public void testInternalSha1PerformanceRTJarOnMarcelsMachine() {
        if (!PATH_TO_RT_JAR.exists()) {
            return;
        }
        String sha1 = toHexString(internal_sha1v2(PATH_TO_RT_JAR));
        // System.out.println(sha1);
    }

    private File createTempoaryFile(String message) throws IOException, FileNotFoundException {
        File f = File.createTempFile("test.sha1", ".txt");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(message.getBytes());
        fos.close();
        return f;
    }
}
