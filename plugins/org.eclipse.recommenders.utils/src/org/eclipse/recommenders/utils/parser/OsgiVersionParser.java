/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.parser;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.Version;

public class OsgiVersionParser implements VersionParser {

    @Override
    public boolean canParse(final String version) {
        try {
            parse(version);
            return true;
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Version parse(final String version) {
        int major = 0;
        int minor = 0;
        int micro = 0;
        String qualifier = "";
        try {
            final StringTokenizer tokenizer = new StringTokenizer(version, ".", true);
            major = parseInt(tokenizer);
            if (tokenizer.hasMoreTokens()) {
                consumeDelimiter(tokenizer);
                minor = parseInt(tokenizer);
                if (tokenizer.hasMoreTokens()) {
                    consumeDelimiter(tokenizer);
                    micro = parseInt(tokenizer);
                    if (tokenizer.hasMoreTokens()) {
                        consumeDelimiter(tokenizer);
                        qualifier = parseString(tokenizer);
                        if (tokenizer.hasMoreTokens()) {
                            Throws.throwIllegalArgumentException("couldn't convert string into version: '%s'", version);
                        }
                    }
                }
            }
        } catch (final NoSuchElementException e) {
            Throws.throwIllegalArgumentException("couldn't convert string into version: '%s'", version);
        }
        return Version.create(major, minor, micro, qualifier);
    }

    private static String parseString(final StringTokenizer st) {
        return st.nextToken();
    }

    private static int parseInt(final StringTokenizer st) {
        return Integer.parseInt(st.nextToken());
    }

    private static void consumeDelimiter(final StringTokenizer st) {
        st.nextToken();
    }

}
