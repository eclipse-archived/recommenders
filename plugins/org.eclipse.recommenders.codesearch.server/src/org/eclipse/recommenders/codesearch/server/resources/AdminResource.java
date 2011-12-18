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
package org.eclipse.recommenders.codesearch.server.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.recommenders.codesearch.FeatureWeights;
import org.eclipse.recommenders.codesearch.Request;
import org.eclipse.recommenders.internal.codesearch.server.IDataAccessService;
import org.eclipse.recommenders.internal.codesearch.server.RequestLogEntry;
import org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneSearchResult;
import org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneSearchService;
import org.eclipse.recommenders.internal.codesearch.server.lucene.ScoringExplanation;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path("admin")
public class AdminResource {
    @Inject
    IDataAccessService db;
    @Inject
    LuceneSearchService search;

    @GET
    @Path("logs")
    public List<RequestLogEntry> getLogs() {
        return db.getLogEntries();
    }

    @Consumes({ APPLICATION_JSON })
    @Produces({ APPLICATION_JSON })
    @GET
    @Path("explain/{requestId}")
    public List<ScoringExplanation> explain(@PathParam("requestId") final String requestId) {
        final List<ScoringExplanation> res = Lists.newLinkedList();
        final RequestLogEntry log = db.getLogEntry(requestId);
        final Request tmp = Request.createEmptyRequest();
        tmp.query = log.query;
        for (final LuceneSearchResult result : log.results) {
            final ScoringExplanation explanation = search.explainScore(tmp, result.luceneDocumentId);
            res.add(explanation);
        }
        return res;
    }

    @Consumes({ APPLICATION_JSON })
    @POST
    @Path("weights")
    public void updateWeights(final FeatureWeights newWeights) {
        search.updateWeights(newWeights);
    }
}
