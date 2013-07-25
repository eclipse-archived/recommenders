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
package org.eclipse.recommenders.utils;

import static org.mockito.Mockito.*;

import java.io.Closeable;
import java.io.IOException;

import org.junit.Test;

public class IOUtilsTest {

    @Test
    public void testCloseQuitely_ThrowingIOException() throws IOException {
        Closeable s = mock(Closeable.class);
        doThrow(new IOException()).when(s).close();
        IOUtils.closeQuietly(s);
    }

    @Test
    public void testCloseQuitely_HappyPath() {
        Closeable s = mock(Closeable.class);
        IOUtils.closeQuietly(s);
    }

    @Test
    public void testCloseQuitely_WithNull() {
        IOUtils.closeQuietly(null);
    }
}
