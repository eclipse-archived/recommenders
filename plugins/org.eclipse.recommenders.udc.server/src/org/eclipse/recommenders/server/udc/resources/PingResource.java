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
package org.eclipse.recommenders.server.udc.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;

import com.google.inject.Inject;

@Path("/ping")
public class PingResource {

    @Inject
    private CouchDBAccessService dataAccess;

    @GET
    public Response ping() {
        final StringBuilder result = new StringBuilder();
        result.append("<html><body><pre>\r\n");
        result.append("Webservice: Online\r\n");

        result.append("CouchDB: ");
        final boolean dataAccessAvailable = dataAccess.isAvailable();
        if (dataAccessAvailable) {
            result.append("Online");
        } else {
            result.append("Offline");
        }

        result.append("\r\n</pre></body></html>");

        if (!dataAccessAvailable) {
            return Response.serverError().entity(result.toString()).build();
        } else {
            return Response.ok(result.toString()).build();
        }
    }
}
