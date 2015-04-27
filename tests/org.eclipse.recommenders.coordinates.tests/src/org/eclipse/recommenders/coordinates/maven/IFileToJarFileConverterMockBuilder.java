/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.coordinates.maven;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

import org.eclipse.recommenders.testing.JarFileMockBuilder;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.Zips.IFileToJarFileConverter;

import com.google.common.base.Optional;

public class IFileToJarFileConverterMockBuilder {

    private final List<Pair<String, Properties>> entries = new LinkedList<Pair<String, Properties>>();

    public IFileToJarFileConverterMockBuilder put(final String pomPropertiesFileName, final Properties properties) {
        entries.add(Pair.newPair(pomPropertiesFileName, properties));
        return this;
    }

    private ByteArrayInputStream createInputStream(final Properties properties) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            properties.store(outputStream, "");
        } catch (IOException e) {
            throwUnhandledException(e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public IFileToJarFileConverter build() {
        return new IFileToJarFileConverter() {

            @Override
            public Optional<JarFile> createJarFile(final File file) {
                final JarFileMockBuilder builder = new JarFileMockBuilder();

                for (Pair<String, Properties> entry : entries) {
                    builder.addEntry(entry.getFirst(), createInputStream(entry.getSecond()));
                }
                return fromNullable(builder.build());
            }
        };
    }

    public static IFileToJarFileConverter createEmptyIFileToJarFileConverter() {
        return new IFileToJarFileConverterMockBuilder().build();
    }
}
