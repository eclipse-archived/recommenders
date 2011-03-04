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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

@Path("source")
public class SourceCodeResource {

    @Inject
    @Named("codesearch.index")
    private File indexFolder;

    @Inject
    private Injector injector;

    @GET
    @Path("{id}")
    public String getSource(@PathParam("id") final String id) {
        final String filename = id.replaceAll("/", ".") + ".java";
        return readFile(filename);
    }

    private String readFile(final String id) {
        final File basedir = new File(indexFolder, "sources");
        try {
            final FileReader reader = new FileReader(new File(basedir, id));
            final BufferedReader bufferedReader = new BufferedReader(reader);
            final StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
