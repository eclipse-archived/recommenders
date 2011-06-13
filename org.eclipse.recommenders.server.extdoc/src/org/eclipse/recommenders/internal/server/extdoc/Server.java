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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jersey.api.client.GenericType;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.ResultObject;
import org.eclipse.recommenders.commons.client.ServerErrorException;
import org.eclipse.recommenders.commons.client.WebServiceClient;

public final class Server {

    // TODO: Temporary fix for demo and tests. Will be replaced with CouchDB
    // client.
    private static final Map<String, Map<String, Object>> CACHE = new HashMap<String, Map<String, Object>>();

    private static final WebServiceClient CLIENT = new WebServiceClient(
            ClientConfiguration.create("http://localhost:5984/extdoc/"));

    private static final String QUOTE;
    private static final String BRACEOPEN;
    private static final String BRACECLOSE;
    private static final String LESSTHAN;
    private static final String GREATERTHAN;

    static {
        QUOTE = encode("\"");
        BRACEOPEN = encode("{");
        BRACECLOSE = encode("}");
        LESSTHAN = encode("<");
        GREATERTHAN = encode(">");
    }

    private Server() {
    }

    public static Map<String, Object> getDocument(final String docId) {
        return CACHE.get(docId);
    }

    public static void storeOrUpdateDocument(final String docId, final Map<String, Object> attributes) {
        CACHE.put(docId, attributes);
    }

    public static <T> T getType(final String providerId, final String type,
            final GenericType<GenericResultObjectView<T>> resultType) {
        final String path = String.format(
                "_design/providers/_view/providers?key=%s%sproviderId%s:%s%s%s,%stype%s:%s%s%s%s", BRACEOPEN, QUOTE,
                QUOTE, QUOTE, providerId, QUOTE, QUOTE, QUOTE, QUOTE, type, QUOTE, BRACECLOSE);
        try {
            final List<ResultObject<T>> rows = CLIENT.doGetRequest(path, resultType).rows;
            return rows.isEmpty() ? null : rows.get(0).value;
        } catch (final ServerErrorException e) {
            return null;
        }
    }

    public static <T> T getMethod(final String providerId, final String method,
            final GenericType<GenericResultObjectView<T>> resultType) {
        final String encodedMethod = method.replace("<", LESSTHAN).replace(">", GREATERTHAN);
        final String path = String.format(
                "_design/providers/_view/providers?key=%s%sproviderId%s:%s%s%s,%smethod%s:%s%s%s%s", BRACEOPEN, QUOTE,
                QUOTE, QUOTE, providerId, QUOTE, QUOTE, QUOTE, QUOTE, encodedMethod, QUOTE, BRACECLOSE);
        try {
            final List<ResultObject<T>> rows = CLIENT.doGetRequest(path, resultType).rows;
            return rows.isEmpty() ? null : rows.get(0).value;
        } catch (final ServerErrorException e) {
            return null;
        }
    }

    protected static void post(final Object object) {
        CLIENT.doPostRequest("", object);
    }

    private static String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
