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

import static java.net.URLEncoder.encode;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.eclipse.recommenders.net.Proxies.*;
import static org.eclipse.recommenders.utils.Urls.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.eclipse.core.internal.net.ProxySelector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.transport.ecf.RepositoryTransport;
import org.eclipse.recommenders.utils.Logs;

@SuppressWarnings("restriction")
public class NetworkCommunicationTestJob extends Job {

    private static final String REQUEST_URL = "http://download.eclipse.org/stats/recommenders/network-communication-test/{0}/java-{1}/{2}-{3}/{4}/"; //$NON-NLS-1$
    private static final String APACHE_HTTP_REQUEST_PART = "apache"; //$NON-NLS-1$
    private static final String P2_HTTP_REQUEST_PART = "p2"; //$NON-NLS-1$
    private static final String UNKNOWN = "unknown"; //$NON-NLS-1$

    public NetworkCommunicationTestJob() {
        super(Messages.JOB_NAME_NETWORK_COMMUNCIATION_TEST);
        setSystem(true);
        setPriority(Job.DECORATE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, Messages.TASK_NETWORK_COMMUNICATION_TEST, 2);

        String javaVersion = getUrlSafeProperty("java.version"); //$NON-NLS-1$
        String operatingSystem = getUrlSafeProperty("os.name"); //$NON-NLS-1$
        String operatingSystemVersion = getUrlSafeProperty("os.version"); //$NON-NLS-1$
        String proxyProvider = ProxySelector.getDefaultProvider();

        doApacheHeadRequest(
                toUri(toUrl(format(REQUEST_URL, APACHE_HTTP_REQUEST_PART, javaVersion, operatingSystem,
                        operatingSystemVersion, proxyProvider))), progress.newChild(1));
        doP2HeadRequest(
                toUri(toUrl(format(REQUEST_URL, P2_HTTP_REQUEST_PART, javaVersion, operatingSystem,
                        operatingSystemVersion, proxyProvider))), progress.newChild(1));

        return Status.OK_STATUS;
    }

    private void doApacheHeadRequest(URI uri, SubMonitor progress) {
        try {
            Executor executor = Executor.newInstance();
            Request request = Request.Head(uri).viaProxy(getProxyHost(uri).orNull());
            proxyAuthentication(executor, uri).execute(request);
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

    private static String getUrlSafeProperty(String key) {
        try {
            return encode(System.getProperty(key, UNKNOWN), UTF_8);
        } catch (UnsupportedEncodingException e) {
            return UNKNOWN;
        }
    }
}
