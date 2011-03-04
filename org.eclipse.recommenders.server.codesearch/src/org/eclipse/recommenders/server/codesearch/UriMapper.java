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
package org.eclipse.recommenders.server.codesearch;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class UriMapper {

    public URI map(final URI uri) {
        final String scheme = uri.getScheme();

        if (scheme.equals("local")) {
            try {
                return new URI("http://localhost:80/codesearch/source/" + encode(uri.getSchemeSpecificPart()));
            } catch (final URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        throw new IllegalStateException("No mapping implemented for scheme: " + scheme);
    }

    private String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
