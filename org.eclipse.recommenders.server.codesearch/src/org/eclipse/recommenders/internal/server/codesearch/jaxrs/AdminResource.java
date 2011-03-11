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
package org.eclipse.recommenders.internal.server.codesearch.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.internal.server.codesearch.IDataAccessService;
import org.eclipse.recommenders.internal.server.codesearch.RequestLogEntry;
import org.eclipse.recommenders.internal.server.codesearch.lucene.LuceneSearchService;
import org.eclipse.recommenders.internal.server.codesearch.lucene.ScoringExplanation;

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
    @POST
    @Path("explain")
    public ScoringExplanation explain(final Request query, final int luceneDocumentId) {
        return search.explainScore(query, luceneDocumentId);
    }
}
