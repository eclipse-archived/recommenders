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
package org.eclipse.recommenders.internal.rcp.codesearch.client;

import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.ICodeSearchResource;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.Response;

import com.google.inject.Inject;

public class CodeSearchClient implements ICodeSearchResource {

    @Inject
    private WebServiceClient client;

    @Override
    public Response search(final Request request) {
        return client.doPostRequest("search", request, Response.class);
    }

    @Override
    public void addFeedback(final String requestId, final Feedback feedback) {
        client.doPostRequest("feedback/" + requestId, feedback);
    }

}
