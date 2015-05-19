/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Johannes Dorn - initial API and implementation
 */
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.utils.Urls.*;

import java.io.FileNotFoundException;
import java.net.URI;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.transport.ecf.RepositoryTransport;
import org.eclipse.recommenders.net.Proxies;
import org.eclipse.recommenders.utils.Logs;

@SuppressWarnings("restriction")
public class NetworkCommunicationTestJob extends Job {

    private static final String REQUEST_URL_PREFIX = "http://download.eclipse.org/stats/recommenders/network-communication-test"; //$NON-NLS-1$
    private static final String APACHE_HTTP_REQUEST_URL = REQUEST_URL_PREFIX + "/apache"; //$NON-NLS-1$
    private static final String P2_HTTP_REQUEST_URL = REQUEST_URL_PREFIX + "/p2"; //$NON-NLS-1$

    public NetworkCommunicationTestJob() {
        super(Messages.JOB_NETWORK_COMMUNCIATION_TEST);
        setSystem(true);
        setPriority(Job.DECORATE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, Messages.TASK_NETWORK_COMMUNICATION_TEST, 2);

        doApacheHeadRequest(toUri(toUrl(APACHE_HTTP_REQUEST_URL)), progress.newChild(1));
        doP2HeadRequest(toUri(toUrl(P2_HTTP_REQUEST_URL)), progress.newChild(1));

        return Status.OK_STATUS;
    }

    private void doApacheHeadRequest(URI uri, SubMonitor progress) {
        try {
            Executor executor = Executor.newInstance();
            Request request = Request.Head(uri);
            Proxies.proxy(executor, uri).execute(request);
        } catch (Exception e) {
            Logs.log(LogMessages.ERROR_ON_APACHE_HEAD_REQUEST, e, uri);
        }
        progress.done();
    }

    private void doP2HeadRequest(URI uri, SubMonitor progress) {
        try {
            RepositoryTransport transport = new RepositoryTransport();
            transport.getLastModified(uri, progress);
        } catch (FileNotFoundException e) {
            // Expected exception.
        } catch (Exception e) {
            Logs.log(LogMessages.ERROR_ON_P2_HEAD_REQUEST, e, uri);
        }
    }
}
