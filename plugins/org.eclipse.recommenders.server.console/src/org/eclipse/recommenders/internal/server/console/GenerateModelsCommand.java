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

import static org.eclipse.recommenders.internal.server.console.Activator.BUNDLE_ID;

import java.io.File;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.mining.calls.Algorithm;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.couch.CouchGuiceModule;
import org.eclipse.recommenders.mining.calls.data.couch.ModelSpecsGenerator;
import org.eclipse.recommenders.server.ServerConfiguration;
import org.eclipse.recommenders.utils.rcp.LoggingUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class GenerateModelsCommand implements Callable<IStatus> {

    @Override
    public IStatus call() throws Exception {
        new WorkspaceJob("Code Recommendes Model Generation") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                monitor.beginTask("Generating...", 5);
                try {
                    generateCallModels(monitor);
                    generateExtdocs(monitor);
                } catch (final Exception x) {
                    return new Status(IStatus.ERROR, "org.eclipse.recommenders.rcp", "Error during mode generation.", x);
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }

            private void generateExtdocs(final IProgressMonitor monitor) {
                monitor.subTask("Creating extdoc models from available data...");
                final org.eclipse.recommenders.mining.extdocs.AlgorithmParameters arguments = new org.eclipse.recommenders.mining.extdocs.AlgorithmParameters();
                final Injector injector = Guice
                        .createInjector(new org.eclipse.recommenders.mining.extdocs.couch.CouchGuiceModule(arguments));
                injector.getInstance(org.eclipse.recommenders.mining.extdocs.Algorithm.class).run();
                monitor.worked(2);
            }

            private void generateCallModels(final IProgressMonitor monitor) {
                final File out = new File(ServerConfiguration.getDataBasedir(), "models/calls/");
                out.mkdirs();
                final AlgorithmParameters arguments = new AlgorithmParameters();
                arguments.setOut(out);
                arguments.setForce(true);
                monitor.subTask("Updating model specifications for all known libraries...");
                final Injector injector = Guice.createInjector(new CouchGuiceModule(arguments));
                injector.getInstance(ModelSpecsGenerator.class).execute();
                monitor.worked(1);
                monitor.subTask("Creating call models for available data...");
                injector.getInstance(Algorithm.class).run();
                monitor.worked(2);
            }
        }.schedule();
        return LoggingUtils.newInfo(BUNDLE_ID, "Scheduled model generation job. Track progress via progress view");
    }

}
