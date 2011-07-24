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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.ServerErrorException;
import org.eclipse.recommenders.commons.client.ServerUnreachableException;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IServerType;
import org.eclipse.recommenders.rcp.extdoc.preferences.PreferenceConstants;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.GenericType;

final class CouchDbServer implements ICouchDbServer {

    private static final String S_METHOD = "method";
    private static final String S_TYPE = "type";

    private static final String QUOTE = encode("\"");
    private static final String BRACEOPEN = encode("{");
    private static final String BRACECLOSE = encode("}");

    private final ClientConfiguration clientConfig;
    private WebServiceClient lazyClient;

    @Inject
    CouchDbServer(
            @Named(PreferenceConstants.NAME_EXTDOC_WEBSERVICE_CONFIGURATION) final ClientConfiguration clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public <T> List<T> getRows(final String view, final Map<String, String> keyParts,
            final GenericType<GenericResultObjectView<T>> resultType) {
        try {
            return getClient().doGetRequest(buildPath(view, keyParts), resultType).getTransformedResult();
        } catch (final ServerErrorException e) {
            return null;
        } catch (final ServerUnreachableException e) {
            return null;
        }
    }

    @Override
    public void post(final IServerType object) {
        getClient().doPostRequest("", object);
    }

    @Override
    public TransactionResult put(final String view, final String documentId, final IServerType object) {
        return getClient().doPutRequest(documentId, object, TransactionResult.class);
    }

    @Override
    public <T> T getProviderContent(final String providerId, final IName element,
            final GenericType<GenericResultObjectView<T>> resultType) {
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
        return String.format("%s%s", path.toString(), "&stale=update_after");
    }

    private static String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private WebServiceClient getClient() {
        if (lazyClient == null) {
            Checks.ensureIsNotNull(clientConfig,
                    "ClientConfiguration was not injected. Check your guice configuration.");
            lazyClient = new WebServiceClient(clientConfig);
        }
        return lazyClient;
    }
}
