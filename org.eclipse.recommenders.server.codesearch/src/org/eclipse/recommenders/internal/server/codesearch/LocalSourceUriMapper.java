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
package org.eclipse.recommenders.internal.server.codesearch;

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.net.URI;
import java.net.URLEncoder;

public class LocalSourceUriMapper implements ISourceUriMapper {

    @Override
    public URI map(final URI uri) {

        try {
            final String part = uri.getSchemeSpecificPart();
            final String encodedPart = URLEncoder.encode(part, "UTF-8");
            final String url = format("%s/source/%s", Constants.WEB_BASE_URL, encodedPart);
            return new URI(url);
        } catch (final Exception e) {
            throw throwUnhandledException(e);
        }

    }
}
