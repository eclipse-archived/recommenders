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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.Response;

@Path("/test")
public class CodeSearchResource {

    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @POST
    public Response search(final Request request) {

        return null;
    }

    @Path("{requestId}")
    @Consumes({ "application/json" })
    @POST
    public void addFeedback(@PathParam("requestId") final String requestId, final Feedback feedback) {

    }
}
