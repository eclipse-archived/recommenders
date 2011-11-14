/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.calls.rcp.CallsCompletionModule.UdcServer;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientHandlerException;

public class CompilationUnitServerExporter implements ICompilationUnitExporter {

    private final static int FRAGMENT_SIZE = 100;
    private final static String webResourcePath = "upload/compilationunit/";

    private final WebServiceClient wsClient;
    private IProgressMonitor monitor;

    @Inject
    public CompilationUnitServerExporter(@UdcServer final ClientConfiguration config) {
        wsClient = new WebServiceClient(config);
        wsClient.enableGzipCompression(true);
    }

    @Override
    public void exportUnits(final IProject sourceProject, final List<CompilationUnit> units,
            final IProgressMonitor monitor) {
        this.monitor = monitor;
        monitor.beginTask("Sending compilation units to server", units.size());
        monitor.subTask("Uploading compilation units for project " + sourceProject.getName());

        trySendData(units);
        monitor.done();
    }

    private void trySendData(final List<CompilationUnit> units) {
        try {
            final List<List<CompilationUnit>> partitions = Lists.partition(units, FRAGMENT_SIZE);
            for (final List<CompilationUnit> partition : partitions) {
                wsClient.doPostRequest(webResourcePath, partition);
                monitor.worked(partition.size());
            }
        } catch (final ClientHandlerException e) {
            throw new IllegalStateException("Could not send compilation units to the server", e);
        }
    }

    @Override
    public void done() {
    }
}
