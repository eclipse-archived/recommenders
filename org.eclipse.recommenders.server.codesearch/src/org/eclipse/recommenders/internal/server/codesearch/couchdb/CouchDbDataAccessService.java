/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.codesearch.couchdb;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.internal.server.codesearch.IDataAccessService;
import org.eclipse.recommenders.internal.server.codesearch.RequestLogEntry;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource.Builder;

public class CouchDbDataAccessService implements IDataAccessService {

    private static String escapedQuotation;
    private static String emptyObject;

    static {
        try {
            escapedQuotation = URLEncoder.encode("\"", "UTF-8");
            emptyObject = URLEncoder.encode("{}", "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throwUnhandledException(e);
        }
    }

    private final String baseUrl = "http://localhost:5984/codesearch/";
    private final Client client = new Client();

    private Builder createRequestBuilder(final String path) {
        return client.resource(baseUrl + path).accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON);
    }

    private String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TransactionResult save(final RequestLogEntry request) {
        final Builder builder = createRequestBuilder("");
        final TransactionResult result = builder.post(TransactionResult.class, request);
        return result;
    }

    @Override
    public RequestLogEntry getLogEntry(final String requestId) {
        final Builder builder = createRequestBuilder(encode(requestId));
        return builder.get(RequestLogEntry.class);
    }

    @Override
    public SnippetSummary getCodeSnippet(final String snippetId) {
        final Builder builder = createRequestBuilder(encode(snippetId));
        return builder.get(SnippetSummary.class);
    }

    @Override
    public List<RequestLogEntry> getLogEntries() {
        final List<RequestLogEntry> logs = query("_design/feedbacks/_view/feedbacks",
                new GenericType<GenericResultObjectView<RequestLogEntry>>() {
                });
        return logs;
    }

    private <T> List<T> query(final String path, final GenericType<GenericResultObjectView<T>> typeToken) {
        final Builder builder = createRequestBuilder(path);
        final GenericResultObjectView<T> result = builder.get(typeToken);
        return transform(result.rows);
    }

    private static <T> List<T> transform(final List<ResultObject<T>> rows) {
        final List<T> result = Lists.newLinkedList();
        for (final ResultObject<T> obj : rows) {
            result.add(obj.value);
        }
        return result;
    }
}
