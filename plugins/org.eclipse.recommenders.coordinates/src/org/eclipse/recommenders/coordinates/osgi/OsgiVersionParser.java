/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Olav Lenz - adapt for the use in the new api.
 */
package org.eclipse.recommenders.coordinates.osgi;

import static com.google.common.base.Optional.*;

import java.util.StringTokenizer;

import com.google.common.base.Optional;

public final class OsgiVersionParser {

    public static Optional<String> parse(final String version) {
        int major = 0;
        int minor = 0;
        int micro = 0;
        try {
            final StringTokenizer tokenizer = new StringTokenizer(version, ".", true);
            major = parseInt(tokenizer);
            if (tokenizer.hasMoreTokens()) {
                consumeDelimiter(tokenizer);
                minor = parseInt(tokenizer);
                if (tokenizer.hasMoreTokens()) {
                    consumeDelimiter(tokenizer);
                    micro = parseInt(tokenizer);
                }
            }
        } catch (final RuntimeException e) {
            return absent();
        }
        return of(major + "." + minor + "." + micro);
    }

    private static int parseInt(final StringTokenizer st) {
        String token = st.nextToken();
        return Integer.parseInt(token);
    }

    private static void consumeDelimiter(final StringTokenizer st) {
        st.nextToken();
    }

    private OsgiVersionParser() {
    }
}
