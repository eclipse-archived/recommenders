/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.server.udc.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Path("/upload")
public class UploadResource {

    @Inject
    private CouchDBAccessService couchdb;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/compilationunit")
    @POST
    public void upload(final CompilationUnit[] units) {
        couchdb.save(units);
        logger.info("Received {} compilations units.", units.length);
    }
}
