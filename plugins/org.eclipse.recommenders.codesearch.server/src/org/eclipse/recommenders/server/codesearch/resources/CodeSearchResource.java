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
package org.eclipse.recommenders.server.codesearch.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.recommenders.codesearch.Feedback;
import org.eclipse.recommenders.codesearch.Proposal;
import org.eclipse.recommenders.codesearch.Request;
import org.eclipse.recommenders.codesearch.Response;
import org.eclipse.recommenders.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.internal.server.codesearch.IDataAccessService;
import org.eclipse.recommenders.internal.server.codesearch.ISourceUriMapper;
import org.eclipse.recommenders.internal.server.codesearch.RequestLogEntry;
import org.eclipse.recommenders.internal.server.codesearch.lucene.LuceneSearchResult;
import org.eclipse.recommenders.internal.server.codesearch.lucene.LuceneSearchService;
import org.eclipse.recommenders.utils.Checks;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path("/")
public class CodeSearchResource {

    @Inject
    private IDataAccessService db;
    @Inject
    private LuceneSearchService searchService;
    @Inject
    private Map<String, ISourceUriMapper> uriMappers;
    private StopWatch stopwatch;

    @Consumes({ APPLICATION_JSON })
    @Produces({ APPLICATION_JSON })
    @POST
    @Path("search")
    public Response search(final Request request) {
        validate(request);
        //
        startPerformanceMeasurement();
        final List<LuceneSearchResult> searchResults = searchService.search(request);
        final List<Proposal> proposals = createProposals(searchResults);
        final long searchTimeInMillis = stopPerformanceMeasurement();

        final RequestLogEntry log = RequestLogEntry.newEntry(request, searchResults, searchTimeInMillis);
        final TransactionResult dbResult = db.save(log);

        final Response response = Response.newResponse(dbResult.id, proposals);
        return response;
    }

    private void startPerformanceMeasurement() {
        stopwatch = new StopWatch();
        stopwatch.start();
    }

    private long stopPerformanceMeasurement() {
        stopwatch.stop();
        return stopwatch.getTime();
    }

    private void validate(final Request request) {
        Checks.ensureIsNotNull(request, "Request object must not be null");
        Checks.ensureIsNotEmpty(request.issuedBy, "IssuedBy not set in Request");
        Checks.ensureIsNotNull(request.query, "Request Query must not be null");
        Checks.ensureIsNotNull(request.type, "Request Type must not be null");
    }

    private List<Proposal> createProposals(final List<LuceneSearchResult> searchResults) {
        final List<Proposal> proposals = Lists.newLinkedList();
        for (final LuceneSearchResult result : searchResults) {
            final SnippetSummary codeSnippet = db.getCodeSnippet(result.snippetId);
            replaceInternalSourceUriWithExternal(codeSnippet);
            final Proposal proposal = Proposal.newProposal(result.score, codeSnippet);
            proposals.add(proposal);
        }
        return proposals;
    }

    private void replaceInternalSourceUriWithExternal(final SnippetSummary codeSnippet) {
        final URI internalSourceUri = codeSnippet.source;
        final String scheme = internalSourceUri.getScheme();
        final ISourceUriMapper mapper = uriMappers.get(scheme);
        final URI externalSourceUri = mapper.map(internalSourceUri);
        codeSnippet.source = externalSourceUri;
    }

    @Path("feedback/{requestId}")
    @Consumes({ APPLICATION_JSON })
    @POST
    public void addFeedback(@PathParam("requestId") final String requestId, final Feedback feedback) {
        validate(feedback);
        final RequestLogEntry logEntry = db.getLogEntry(requestId);
        appendFeedback(logEntry, feedback);
        logEntry.query.nullEmptySets();
        db.save(logEntry);
    }

    private void validate(final Feedback feedback) {
        Checks.ensureIsNotNull(feedback);
        Checks.ensureIsNotNull(feedback.event);
        Checks.ensureIsNotEmpty(feedback.snippetId, "SnippetId must be set in Feedback");
    }

    private void appendFeedback(final RequestLogEntry logEntry, final Feedback feedback) {
        if (logEntry.feedbacks == null) {
            logEntry.feedbacks = Lists.newLinkedList();
        }
        logEntry.feedbacks.add(feedback);
    }

}
