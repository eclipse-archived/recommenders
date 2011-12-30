/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Marcel Bruch - refactored scattered pieces into single class.
 */
package org.eclipse.recommenders.internal.extdoc.server;

import static com.google.common.base.Optional.fromNullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.ClassSelfcallDirectives;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.internal.extdoc.server.wiring.GuiceModule.ExtDocScope;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;
import org.eclipse.recommenders.webclient.exceptions.ServerErrorException;
import org.eclipse.recommenders.webclient.exceptions.ServerUnreachableException;
import org.eclipse.recommenders.webclient.results.GenericResultObjectView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

// TODO: Review: Johannes, can we we move some of these utility methods to webclient?
public class CouchDataAccess {

    private static final String S_METHOD = "method";
    private static final String S_TYPE = "type";

    private static final String QUOTE = encode("\"");
    private static final String BRACEOPEN = encode("{");
    private static final String BRACECLOSE = encode("}");

    private final WebServiceClient client;

    @Inject
    public CouchDataAccess(@ExtDocScope final ClientConfiguration config) {
        client = new WebServiceClient(config);
    }

    public Optional<MethodSelfcallDirectives> getMethodSelfcallDirectives(final IMethodName method) {
        final MethodSelfcallDirectives res = getProviderContent(MethodSelfcallDirectives.class.getSimpleName(), method,
                new GenericType<GenericResultObjectView<MethodSelfcallDirectives>>() {
                });
        return fromNullable(res);
    }

    private <T> T getProviderContent(final String providerId, final IName element,
            final GenericType<GenericResultObjectView<T>> resultType) {
        Preconditions.checkNotNull(element);
        final String key = element instanceof IMethodName ? S_METHOD : S_TYPE;
        final List<T> rows = getRows("providers",
                ImmutableMap.of("providerId", providerId, key, element.getIdentifier()), resultType);
        return rows == null || rows.isEmpty() ? null : rows.get(0);
    }

    private <T> List<T> getRows(final String view, final Map<String, String> keyParts,
            final GenericType<GenericResultObjectView<T>> resultType) {
        try {
            final String path = buildPath(view, keyParts);
            final GenericResultObjectView<T> response = client.doGetRequest(path, resultType);
            return response.getTransformedResult();
        } catch (final ServerErrorException e) {
            return null;
        } catch (final ServerUnreachableException e) {
            return null;
        }
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

    public Optional<ClassSelfcallDirectives> getClassSelfcallDirectives(final ITypeName type) {
        final ClassSelfcallDirectives res = getProviderContent(ClassSelfcallDirectives.class.getSimpleName(), type,
                new GenericType<GenericResultObjectView<ClassSelfcallDirectives>>() {
                });
        return fromNullable(res);
    }

    public Optional<ClassOverrideDirectives> getClassOverrideDirectives(final ITypeName type) {
        final ClassOverrideDirectives res = getProviderContent(ClassOverrideDirectives.class.getSimpleName(), type,
                new GenericType<GenericResultObjectView<ClassOverrideDirectives>>() {
                });

        return fromNullable(res);
    }

    public Optional<ClassOverridePatterns> getClassOverridePatterns(final ITypeName type) {
        final ClassOverridePatterns res = getProviderContent(ClassOverridePatterns.class.getSimpleName(), type,
                new GenericType<GenericResultObjectView<ClassOverridePatterns>>() {
                });
        return fromNullable(res);
    }
}