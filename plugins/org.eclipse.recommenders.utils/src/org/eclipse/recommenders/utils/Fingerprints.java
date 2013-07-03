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

import static org.eclipse.recommenders.utils.Checks.ensureExists;
import static org.eclipse.recommenders.utils.Checks.ensureIsFile;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

public class Fingerprints {
    private static final class StreamInputSupplier implements InputSupplier<InputStream> {
        private final InputStream stream;

        private StreamInputSupplier(final InputStream stream) {
            this.stream = stream;
        }

        @Override
        public InputStream getInput() throws IOException {
            return stream;
        }
    }

    public static String sha1(final File file) {
        ensureIsNotNull(file);
        ensureExists(file);
        ensureIsFile(file);
        //
        try {
            return Files.hash(file, Hashing.sha1()).toString();
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    public static String sha1(final String message) {
        ensureIsNotNull(message);
        HashCode hash = Hashing.sha1().hashString(message, Charsets.UTF_8);
        return hash.toString();
    }

    public static String sha1(final InputStream stream) {
        ensureIsNotNull(stream);
        try {
            final StreamInputSupplier supplier = new StreamInputSupplier(stream);
            HashCode hash = ByteStreams.hash(supplier, Hashing.sha1());
            return hash.toString();
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private Fingerprints() {
        // this is a utility class - no instances of this class can be created.
    }
}
