/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.recommenders.internal.stacktraces.rcp.Stacktraces.PLUGIN_ID;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.eclipse.core.internal.net.ProxyManager;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.StackTraceEvent;
import org.eclipse.recommenders.utils.gson.GsonUtil;

@SuppressWarnings("restriction")
public class StacktraceUploadJob extends Job {

    private Executor executor = Executor.newInstance();
    private StackTraceEvent event;
    private URI target;

    StacktraceUploadJob(StackTraceEvent event, URI target) {
        super("Sending error log entry to " + target + "...");
        this.event = event;
        this.target = target;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask("Sending error log entry...", 1);
        try {
            updateProxySettings();

            String body = GsonUtil.serialize(event);
            Request request = Request.Post(target).bodyString(body, ContentType.APPLICATION_JSON);
            Response response = executor.execute(request);
            return new Status(IStatus.INFO, PLUGIN_ID,
                    "Reported error log entry to recommenders.eclipse.org. Thank you for your help. "
                            + response.returnContent());
        } catch (Exception e) {
            return new Status(WARNING, PLUGIN_ID, "Failed to send error log entry.", e);
        } finally {
            monitor.done();
        }
    }

    private void updateProxySettings() {
        IProxyData[] proxies = ProxyManager.getProxyManager().select(target);
        if (isEmpty(proxies)) {
            executor.clearAuth();
        } else {
            IProxyData proxy = proxies[0];
            HttpHost host = new HttpHost(proxy.getHost(), proxy.getPort());
            executor.authPreemptiveProxy(host);
            if (proxy.getUserId() != null) {
                String userId = Proxies.getUserName(proxy.getUserId()).orNull();
                String pass = proxy.getPassword();
                String workstation = Proxies.getWorkstation().orNull();
                String domain = Proxies.getUserDomain(proxy.getUserId()).orNull();
                executor.auth(host, userId, pass, workstation, domain);
            }
        }
    }
}
