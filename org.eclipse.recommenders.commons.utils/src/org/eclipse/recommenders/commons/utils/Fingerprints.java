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

import static org.eclipse.recommenders.commons.utils.Checks.ensureExists;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsFile;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Fingerprints {
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
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            final FileChannel channel = stream.getChannel();
            final ByteBuffer buffer = ByteBuffer.allocateDirect(8 * 1024);
            final MessageDigest digest = createMessageDigest();
            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer);
                buffer.clear();
            }
            return digest.digest();
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public static String sha1(final File file) {
        ensureIsNotNull(file);
        ensureExists(file);
        ensureIsFile(file);
        //
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            final FileChannel channel = stream.getChannel();
            final ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, (int) channel.size());
            final MessageDigest digest = createMessageDigest();
            digest.update(buffer);
            return toString(digest);
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        } finally {
            IOUtils.closeQuietly(stream);
            // it's sufficient to close the stream only.
            // IOUtils.closeQuietly(channel);
        }
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
            final DigestInputStream digestStream = new DigestInputStream(new BufferedInputStream(stream), digest);
            while (digestStream.read() != -1) {
                // simply read the stream data one-by-one
                // XXX this may be slow. Maybe we should use byte[] to read more
                // data in a single pass?
            }
            return toString(digest);
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
