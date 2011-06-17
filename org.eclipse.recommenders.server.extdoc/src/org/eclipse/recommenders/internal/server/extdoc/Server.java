/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.ResultObject;
import org.eclipse.recommenders.commons.client.ServerErrorException;
import org.eclipse.recommenders.commons.client.ServerUnreachableException;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.internal.rcp.extdoc.preferences.PreferenceConstants;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.GenericType;

public final class Server {

    @Inject
    @Named(PreferenceConstants.NAME_EXTDOC_WEBSERVICE_CONFIGURATION)
    private static ClientConfiguration clientConfig;
    private static WebServiceClient lazyClient;

    private static final String QUOTE;
    private static final String BRACEOPEN;
    private static final String BRACECLOSE;

    static {
        QUOTE = encode("\"");
        BRACEOPEN = encode("{");
        BRACECLOSE = encode("}");
    }

    private Server() {
    }

    public static <T> T getProviderContent(final String providerId, final String key, final String value,
            final GenericType<GenericResultObjectView<T>> resultType) {
        final String path = String.format(
                "_design/providers/_view/providers?key=%s%sproviderId%s:%s%s%s,%s%s%s:%s%s%s%s&stale=ok", BRACEOPEN,
                QUOTE, QUOTE, QUOTE, providerId, QUOTE, QUOTE, key, QUOTE, QUOTE, encode(value), QUOTE, BRACECLOSE);
        try {
            final List<ResultObject<T>> rows = getClient().doGetRequest(path, resultType).rows;
            return rows.isEmpty() ? null : rows.get(0).value;
        } catch (final ServerErrorException e) {
            return null;
        } catch (final ServerUnreachableException e) {
            return null;
        }
    }

    public static void post(final Object object) {
        getClient().doPostRequest("", object);
    }

    private static String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static WebServiceClient getClient() {
        if (lazyClient == null) {
            Checks.ensureIsNotNull(clientConfig,
                    "ClientConfiguration was not injected. Check your guice configuration.");
            lazyClient = new WebServiceClient(clientConfig);
        }
        return lazyClient;
    }
}
