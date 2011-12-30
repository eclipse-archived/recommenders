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
package org.eclipse.recommenders.internal.extdoc.server.proxy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.recommenders.internal.extdoc.server.proxy.GuiceModule.ExtDocScope;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;
import org.eclipse.recommenders.webclient.exceptions.ServerErrorException;
import org.eclipse.recommenders.webclient.exceptions.ServerUnreachableException;
import org.eclipse.recommenders.webclient.results.GenericResultObjectView;
import org.eclipse.recommenders.webclient.results.TransactionResult;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public class CouchDBAccessService {

    private static final String S_METHOD = "method";
    private static final String S_TYPE = "type";

    private static final String QUOTE = encode("\"");
    private static final String BRACEOPEN = encode("{");
    private static final String BRACECLOSE = encode("}");

    private final WebServiceClient client;

    @Inject
    public CouchDBAccessService(@ExtDocScope final ClientConfiguration config) {
        client = new WebServiceClient(config);
    }

    public <T> List<T> getRows(final String view, final Map<String, String> keyParts,
            final GenericType<GenericResultObjectView<T>> resultType) {
        try {
            return client.doGetRequest(buildPath(view, keyParts), resultType).getTransformedResult();
        } catch (final ServerErrorException e) {
            return null;
        } catch (final ServerUnreachableException e) {
            return null;
        }
    }

    public void post(final Object object) {
        client.doPostRequest("", object);
    }

    public TransactionResult put(final String documentId, final Object object) {
        return client.doPutRequest(documentId, object, TransactionResult.class);
    }

    public <T> T getProviderContent(final String providerId, final IName element,
            final GenericType<GenericResultObjectView<T>> resultType) {
        Preconditions.checkNotNull(element);
        final String key = element instanceof IMethodName ? S_METHOD : S_TYPE;
        final List<T> rows = getRows("providers",
                ImmutableMap.of("providerId", providerId, key, element.getIdentifier()), resultType);
        return rows == null || rows.isEmpty() ? null : rows.get(0);
    }

    private static String buildPath(final String view, final Map<String, String> keyParts) {
        final StringBuilder path = new StringBuilder(32);
        path.append(String.format("_design/providers/_view/%s?key=%s", view, BRACEOPEN));
        for (final Entry<String, String> keyEntry : keyParts.entrySet()) {
            final String value = encode(keyEntry.getValue());
            path.append(String.format("%s%s%s:%s%s%s,", QUOTE, keyEntry.getKey(), QUOTE, QUOTE, value, QUOTE));
        }
        path.replace(path.length() - 1, path.length(), BRACECLOSE);
        // remove stale=update_after
        // return String.format("%s%s", path.toString(), "&stale=update_after");
        return String.format("%s%s", path.toString(), "");
    }

    private static String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}