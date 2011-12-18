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
package org.eclipse.recommenders.internal.codesearch.server;

import static java.lang.String.format;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.internal.codesearch.server.wiring.GuiceModule.CodesearchBaseurl;

import com.google.inject.Inject;

public class LocalSourceUriMapper implements ISourceUriMapper {

    private final String baseurl;

    @Inject
    public LocalSourceUriMapper(@CodesearchBaseurl final URL baseurl) {
        this.baseurl = StringUtils.removeEnd(baseurl.toExternalForm(), "/");
    }

    @Override
    public URI map(final URI uri) {
        try {
            final String part = uri.getSchemeSpecificPart();
            final String encodedPart = URLEncoder.encode(part, "UTF-8");
            final String url = format("%s/source/%s", baseurl, encodedPart);
            return new URI(url);
        } catch (final Exception e) {
            throw throwUnhandledException(e);
        }
    }
}
