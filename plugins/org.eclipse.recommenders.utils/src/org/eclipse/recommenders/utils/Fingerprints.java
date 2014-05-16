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

import static org.eclipse.recommenders.utils.Checks.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

public final class Fingerprints {

    private Fingerprints() {
        // this is a utility class - no instances of this class can be created.
    }

    public static String sha1(final File file) {
        return hashFile(file, Hashing.sha1());
    }

    public static String md5(final File file) {
        return hashFile(file, Hashing.md5());
    }

    public static String sha1(final String message) {
        return hashString(message, Hashing.sha1());
    }

    public static String md5(final String message) {
        return hashString(message, Hashing.md5());
    }

    public static String sha1(final InputStream stream) {
        return hashStream(stream, Hashing.sha1());
    }

    public static String md5(final InputStream stream) {
        return hashStream(stream, Hashing.md5());
    }

    private static String hashFile(final File file, final HashFunction hashFunction) {
        ensureIsNotNull(file);
        ensureExists(file);
        ensureIsFile(file);
        try {
            return Files.hash(file, hashFunction).toString();
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    private static String hashString(final String message, final HashFunction hashFunction) {
        ensureIsNotNull(message);
        HashCode hash = hashFunction.hashString(message, Charsets.UTF_8);
        return hash.toString();
    }

    private static String hashStream(final InputStream stream, final HashFunction hashFunction) {
        ensureIsNotNull(stream);
        try {
            final StreamInputSupplier supplier = new StreamInputSupplier(stream);
            HashCode hash = ByteStreams.hash(supplier, hashFunction);
            return hash.toString();
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

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
}
