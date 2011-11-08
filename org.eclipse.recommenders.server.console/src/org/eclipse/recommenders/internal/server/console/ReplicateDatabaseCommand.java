/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.server.console;

import static org.eclipse.recommenders.commons.utils.LoggingUtils.newInfo;
import static org.eclipse.recommenders.commons.utils.LoggingUtils.newWarning;
import static org.eclipse.recommenders.internal.server.console.Activator.BUNDLE_ID;

import java.io.File;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.internal.server.console.ConsoleGuiceModule.LocalCouchdb;

import com.google.inject.Inject;

public class ReplicateDatabaseCommand implements Callable<IStatus> {

    private final WebServiceClient client;

    private String db;

    @Inject
    public ReplicateDatabaseCommand(@LocalCouchdb final WebServiceClient client,
            @LocalCouchdb final File couchConfigurationArea) {
        this.client = client;
    }

    public void setDatabaseName(final String name) {
        this.db = name;
    }

    private class Replicate {

        public Replicate(final String source, final String target) {
            this.source = source;
            this.target = target;
        }

        public String source;
        public String target;
    }

    @Override
    public IStatus call() throws Exception {
        final Replicate replicate = new Replicate("http://137.248.121.220:5984/" + db, db);
        new WorkspaceJob("Replicating " + db) {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("replication in progress", IProgressMonitor.UNKNOWN);
                    client.createRequestBuilder("_replicate").post(replicate);
                    return newInfo(BUNDLE_ID, "Triggered replication from '%s' to '%s'.", replicate.source,
                            replicate.target);
                } catch (final Exception e) {
                    return newWarning(e, BUNDLE_ID, "Failed to schedule replication from '%s' to '%s'.",
                            replicate.source, replicate.target);
                } finally {
                    monitor.done();
                }
            }
        }.schedule();
        return newInfo(BUNDLE_ID, "Triggered replication from '%s' to '%s'. Check progress view for.",
                replicate.source, replicate.target);
    }
}
