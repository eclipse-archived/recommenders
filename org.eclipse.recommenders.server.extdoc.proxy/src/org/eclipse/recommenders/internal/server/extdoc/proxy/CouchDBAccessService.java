/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc.proxy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.internal.server.extdoc.proxy.GuiceModule.ExtDocScope;

import com.google.inject.Inject;

public class CouchDBAccessService {

    private final WebServiceClient client;
    private final String escapedQuotation;

    @Inject
    public CouchDBAccessService(@ExtDocScope final ClientConfiguration config) {
        client = new WebServiceClient(config);
        // XXX: We don't support stale views ok yet:
        // if (staledViewsOk) {
        // client.addQueryParameter("stale=update_after");
        // }
        escapedQuotation = encode("\"");
    }

    protected String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}