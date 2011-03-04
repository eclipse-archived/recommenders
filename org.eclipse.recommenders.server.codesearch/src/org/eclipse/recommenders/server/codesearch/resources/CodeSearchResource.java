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

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.Response;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.server.codesearch.RequestLogEntry;
import org.eclipse.recommenders.server.codesearch.SearchResult;
import org.eclipse.recommenders.server.codesearch.SearchService;
import org.eclipse.recommenders.server.codesearch.TransactionResult;
import org.eclipse.recommenders.server.codesearch.UriMapper;
import org.eclipse.recommenders.server.codesearch.couchdb.IDataAccessService;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path("/")
public class CodeSearchResource {

    @Inject
    private IDataAccessService dataAccess;
    @Inject
    private SearchService searchService;
    @Inject
    private UriMapper uriMapper;

    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @POST
    @Path("search")
    public Response search(final Request request) {
        validate(request);
        final RequestLogEntry logEntry = createLogEntry(request);
        final TransactionResult transactionResult = dataAccess.save(logEntry);
        return createResponse(logEntry, transactionResult);
    }

    private void validate(final Request request) {
        Checks.ensureIsNotNull(request, "Request object must not be null");
        Checks.ensureIsNotEmpty(request.issuedBy, "IssuedBy not set in Request");
        Checks.ensureIsNotNull(request.query, "Request Query must not be null");
        Checks.ensureIsNotNull(request.type, "Request Type must not be null");
    }

    private RequestLogEntry createLogEntry(final Request request) {
        final RequestLogEntry logEntry = RequestLogEntry.newEntryFromRequest(request);
        logEntry.issuedOn = new Date();
        final List<SearchResult> searchResults = searchService.search(request);
        logEntry.proposals = createProposals(searchResults);
        return logEntry;
    }

    private List<Proposal> createProposals(final List<SearchResult> searchResults) {
        final List<Proposal> proposals = Lists.newLinkedList();
        for (final SearchResult result : searchResults) {
            final Proposal proposal = Proposal.newProposal();
            proposal.score = result.score;
            proposal.snippet = dataAccess.getCodeSnippet(result.snippetId);
            proposal.snippet.source = uriMapper.map(proposal.snippet.source);
        }
        return proposals;
    }

    private Response createResponse(final RequestLogEntry logEntry, final TransactionResult transactionResult) {
        final Response response = Response.newResponse();
        response.requestId = transactionResult.id;
        response.proposals = logEntry.proposals;
        return response;
    }

    @Path("feedback/{requestId}")
    @Consumes({ "application/json" })
    @POST
    public void addFeedback(@PathParam("requestId") final String requestId, final Feedback feedback) {
        validate(feedback);
        final RequestLogEntry logEntry = dataAccess.getLogEntry(requestId);
        appendFeedback(logEntry, feedback);
        dataAccess.save(logEntry);
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
