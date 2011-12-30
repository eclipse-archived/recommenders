/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.codesearch.rcp;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.codesearch.Proposal;
import org.eclipse.recommenders.codesearch.Request;
import org.eclipse.recommenders.codesearch.Response;

import com.google.common.collect.Lists;

public class RCPResponse {

    public static RCPResponse newInstance(final Request request, final Response serverResponse,
            final IJavaProject typesResolverContext) {
        final RCPResponse res = new RCPResponse();
        res.original = serverResponse;
        res.request = request;
        for (final Proposal proposal : serverResponse.proposals) {
            final RCPProposal rcpProposal = RCPProposal.newProposalFromServerProposal(proposal, typesResolverContext);
            res.proposals.add(rcpProposal);
        }
        return res;
    }

    private Request request;
    private Response original;

    private final List<RCPProposal> proposals = Lists.newArrayList();

    public Request getRequest() {
        return request;
    }

    public List<RCPProposal> getProposals() {
        return proposals;
    }

    public String getRequestId() {
        return original.requestId;
    }

    public boolean isEmpty() {
        return proposals.isEmpty();
    }

    public int getNumberOfProposals() {
        return proposals.size();
    }

}
