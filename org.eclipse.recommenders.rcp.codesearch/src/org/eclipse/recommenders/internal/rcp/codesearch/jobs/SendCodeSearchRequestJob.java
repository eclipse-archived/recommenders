/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.jobs;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.commons.codesearch.ICodeSearchResource;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.Response;
import org.eclipse.recommenders.commons.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.swt.widgets.Display;

public class SendCodeSearchRequestJob extends WorkspaceJob {
    private final Request request;
    private RCPResponse response;
    private final IJavaProject javaProject;
    private final CodeSearchClient searchClient;

    public SendCodeSearchRequestJob(final Request request, final IJavaProject javaProject,
            final CodeSearchClient searchClient) {
        super("Searching Example Code");
        this.javaProject = javaProject;
        this.searchClient = searchClient;
        ensureIsNotNull(request);
        this.request = request;
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        if (!isValidRequest()) {
            return Status.CANCEL_STATUS;
        }
        final StopWatch netWatch = new StopWatch();
        netWatch.start();
        final Response serverResponse = searchClient.search(request);
        response = RCPResponse.newInstance(request, serverResponse, javaProject);
        netWatch.stop();
        System.out.printf("net comm took %s\n", netWatch);
        openViews();
        return Status.OK_STATUS;
    }

    private boolean isValidRequest() {
        return Request.INVALID != request;
    }

    private void openViews() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CodesearchPlugin.showQueryView().setInput(request, javaProject);
                CodesearchPlugin.showExamplesView().setInput(response);
            }
        });
    }

    public static ICodeSearchResource createTransport() {
        return null;
    }
}
