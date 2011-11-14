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

import java.util.Date;
import java.util.List;

import org.eclipse.recommenders.codesearch.Feedback;
import org.eclipse.recommenders.codesearch.Request;
import org.eclipse.recommenders.codesearch.RequestType;
import org.eclipse.recommenders.codesearch.SnippetSummary;
import org.eclipse.recommenders.internal.server.codesearch.lucene.LuceneSearchResult;
import org.eclipse.recommenders.utils.gson.GsonUtil;

import com.google.gson.annotations.SerializedName;

public class RequestLogEntry {

    /**
     * Request id. Created by the server.
     */
    @SerializedName("_id")
    public String id;

    @SerializedName("_rev")
    public String rev;

    /**
     * The date the query was issued on. This field is set by the server and
     * used for logging purpose only.
     */
    public Date issuedOn;

    /**
     * The unique user-id is generated from the user's mac address and hashed
     * using sha1. This is used internally for filtering operations - for
     * instance to determine new or frequent users of the system etc.)
     */
    public String issuedBy;

    /**
     * Different types of queries can be issued. Since each query type may use
     * different feature weights, this flags allows the server to quickly
     * identify the feature weight set to use.
     */
    public RequestType type;

    /**
     * This summary contains the information used to find relevant code
     * examples. It is typically created from a text selection of an Editor
     * inside Eclipse.
     */
    public SnippetSummary query;

    public long searchTimeInMillis;

    /**
     * The proposals made by the server.
     */
    public List<LuceneSearchResult> results;

    /**
     * The feedbacks collected on server side. This field used for logs only and
     * thus is typically <code>null</code> on client side.
     */
    public List<Feedback> feedbacks;

    public static RequestLogEntry newEntry(final Request request) {
        final RequestLogEntry res = new RequestLogEntry();
        res.issuedBy = request.issuedBy;
        res.query = request.query.clone();
        res.query.nullEmptySets();
        res.type = request.type;
        res.issuedOn = new Date();
        return res;
    }

    public static RequestLogEntry newEntry(final Request request, final List<LuceneSearchResult> searchResults,
            final long searchTimeInMillis) {
        final RequestLogEntry res = newEntry(request);
        res.results = searchResults;
        res.searchTimeInMillis = searchTimeInMillis;
        return res;
    }

    @Override
    public String toString() {
        return GsonUtil.serialize(this);
    }

}
