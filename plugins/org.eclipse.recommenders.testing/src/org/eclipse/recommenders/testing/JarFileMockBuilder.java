/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Andreas Sewe - enhancements to mock's fidelity
 *    Johannes Dorn - mock getEntry(String)
 */
package org.eclipse.recommenders.testing;

import static org.eclipse.recommenders.utils.Throws.throwUnreachable;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class JarFileMockBuilder {

    private final JarFile jarFile = mock(JarFile.class);

    private final List<JarEntry> mockedEntries = new LinkedList<>();

    public JarFile build() {
        when(jarFile.entries()).thenAnswer(new Answer<Enumeration<JarEntry>>() {

            @Override
            public Enumeration<JarEntry> answer(InvocationOnMock invocation) throws Throwable {
                return Collections.enumeration(mockedEntries);
            }
        });
        return jarFile;
    }

    public void addEntry(final String name, final InputStream stream) {
        JarEntry entry = mock(JarEntry.class);
        when(entry.getName()).thenReturn(name);
        when(jarFile.getEntry(name)).thenReturn(entry);
        try {
            when(jarFile.getInputStream(entry)).thenReturn(stream);
        } catch (IOException e) {
            throwUnreachable();
        }
        mockedEntries.add(entry);
    }
}
