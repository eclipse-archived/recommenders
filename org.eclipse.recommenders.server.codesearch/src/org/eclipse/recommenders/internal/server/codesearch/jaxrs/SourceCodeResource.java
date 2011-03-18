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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.recommenders.internal.server.codesearch.ZipFileSourceCodeProvider;

import com.google.inject.Inject;

@Path("source")
public class SourceCodeResource {

    @Inject
    private ZipFileSourceCodeProvider localFileService;

    @GET
    @Path("{id}")
    public String getSource(@PathParam("id") final String id) {
        final String filename = id.replaceAll("\\W", ".").substring(1) + ".java";
        final String fileContent = localFileService.readFile(filename);
        if (fileContent == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            return fileContent;
        }
    }

}
