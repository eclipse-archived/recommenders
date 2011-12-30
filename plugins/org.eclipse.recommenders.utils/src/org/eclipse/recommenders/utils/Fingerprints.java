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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

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

    // private static final String DIGEST_MD5 = "MD5";
    private static final String DIGEST_SHA1 = "SHA-1";

    private static MessageDigest createMessageDigest() throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance(DIGEST_SHA1);
        return digest;
    }

    public static byte[] internal_sha1v2(final File file) {
        ensureIsNotNull(file);
        ensureExists(file);
        ensureIsFile(file);
        //
        try {
            final MessageDigest digest = createMessageDigest();
            return Files.getDigest(file, digest);
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    public static String sha1(final File file) {
        final byte[] sha1 = internal_sha1v2(file);
        return toHexString(sha1);
    }

    private static String toString(final MessageDigest digest) {
        final byte[] res = digest.digest();
        return toHexString(res);
    }

    public static String sha1(final String message) {
        ensureIsNotNull(message);
        //
        try {
            final MessageDigest digest = createMessageDigest();
            digest.update(message.getBytes());
            return toString(digest);
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    public static String sha1(final InputStream stream) {
        ensureIsNotNull(stream);
        try {
            final MessageDigest digest = createMessageDigest();
            final StreamInputSupplier supplier = new StreamInputSupplier(stream);
            final byte[] sha1 = ByteStreams.getDigest(supplier, digest);
            return toHexString(sha1);
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private static String toHexString(final byte[] hash) {
        ensureIsNotNull(hash);
        // this is said to be very slow... - we may look at this if hashing
        // actually takes too long.
        final Formatter formatter = new Formatter();
        for (final byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    // private static String sha1AsHex(final String message) {
    // ensureIsNotNull(message);
    // //
    // try {
    // final MessageDigest digest = createMessageDigest();
    // digest.update(message.getBytes());
    // final byte[] res = digest.digest();
    // return toHexString(res);
    // } catch (final Exception e) {
    // throw Throws.throwUnhandledException(e);
    // }
    // }

    private Fingerprints() {
        // this is a utility class - no instances of this class can be created.
    }
}
