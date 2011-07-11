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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.ResultObject;
import org.eclipse.recommenders.commons.client.ServerErrorException;
import org.eclipse.recommenders.commons.client.ServerUnreachableException;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.rcp.extdoc.preferences.PreferenceConstants;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.GenericType;

public final class Server {

    @Inject
    @Named(PreferenceConstants.NAME_EXTDOC_WEBSERVICE_CONFIGURATION)
    private static ClientConfiguration clientConfig;
    private static WebServiceClient lazyClient;

    @Inject
    private static JavaElementResolver resolver;

    private static final String QUOTE = encode("\"");
    private static final String BRACEOPEN = encode("{");
    private static final String BRACECLOSE = encode("}");

    private Server() {
    }

    static <T> T get(final String path, final Class<T> resultType) {
        try {
            System.err.println(path);
            return getClient().doGetRequest(path, resultType);
        } catch (final ServerErrorException e) {
            return null;
        } catch (final ServerUnreachableException e) {
            return null;
        }
    }

    static <T> List<T> getRows(final String path, final GenericType<GenericResultObjectView<T>> resultType) {
        try {
            final List<T> results = new ArrayList<T>();
            final GenericResultObjectView<T> rows = getClient().doGetRequest(path, resultType);
            for (final ResultObject<T> resultObject : rows.rows) {
                results.add(resultObject.value);
            }
            return results;
        } catch (final ServerErrorException e) {
            return null;
        } catch (final ServerUnreachableException e) {
            return null;
        }
    }

    public static void post(final Object object) {
        getClient().doPostRequest("", object);
    }

    static <T> T getProviderContent(final String view, final String providerId, final String key, final String value,
            final Class<T> resultType) {
        final String path = buildPath(view, ImmutableMap.of("providerId", providerId, key, value));
        return get(path, resultType);
    }

    public static <T> T getProviderContent(final String providerId, final String key, final String value,
            final GenericType<GenericResultObjectView<T>> resultType) {
        return getProviderContent("providers", providerId, key, value, resultType);
    }

    public static <T> T getProviderContent(final String view, final String providerId, final String key,
            final String value, final GenericType<GenericResultObjectView<T>> resultType) {
        final String path = buildPath(view, ImmutableMap.of("providerId", providerId, key, value));
        final List<T> rows = getRows(path, resultType);
        return rows == null || rows.isEmpty() ? null : rows.get(0);
    }

    public static String createKey(final IMethod method) {
        final IMethodName methodName = resolver.toRecMethod(method);
        return methodName == null ? null : methodName.getIdentifier();
    }

    public static String createKey(final IType type) {
        final ITypeName typeName = resolver.toRecType(type);
        return typeName == null ? null : typeName.getIdentifier();
    }

    static String buildPath(final String view, final Map<String, String> key) {
        final StringBuilder path = new StringBuilder();
        path.append(String.format("_design/providers/_view/%s?key=%s", view, BRACEOPEN));
        for (final Entry<String, String> keyEntry : key.entrySet()) {
            path.append(String.format("%s%s%s:%s%s%s,", QUOTE, keyEntry.getKey(), QUOTE, QUOTE,
                    encode(keyEntry.getValue()), QUOTE));
        }
        path.replace(path.length() - 1, path.length(), BRACECLOSE);
        return String.format("%s%s", path.toString(), "&stale=ok");
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

    public static void setConfig(final ClientConfiguration clientConfig, final JavaElementResolver resolver) {
        Server.clientConfig = clientConfig;
        Server.resolver = Checks.ensureIsNotNull(resolver);
    }
}
