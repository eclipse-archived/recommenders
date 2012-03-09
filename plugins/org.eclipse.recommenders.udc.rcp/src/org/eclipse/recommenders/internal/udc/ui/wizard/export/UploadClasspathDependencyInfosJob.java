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
package org.eclipse.recommenders.internal.udc.ui.wizard.export;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.internal.completion.rcp.calls.store3.ClasspathDependencyStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionModule.CallModelsServer;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;

public class UploadClasspathDependencyInfosJob extends Job {

    private final ClasspathDependencyStore dependencyStore;
    private final WebServiceClient client;

    @Inject
    public UploadClasspathDependencyInfosJob(final ClasspathDependencyStore dependencyStore,
            @CallModelsServer final ClientConfiguration config) {
        super("Uploading dependency meta data");
        this.dependencyStore = dependencyStore;
        client = new WebServiceClient(config);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        final Set<File> dependencies = dependencyStore.getFiles();
        monitor.beginTask("", dependencies.size());

        for (final File dependency : dependencies) {
            if (dependencyStore.containsClasspathDependencyInfo(dependency)) {
                final ClasspathDependencyInformation dependencyInfo = dependencyStore
                        .getClasspathDependencyInfo(dependency);
                client.doPostRequest("dependencyInfo", dependencyInfo);
            }
            monitor.worked(1);
        }
        return Status.OK_STATUS;
    }

}
