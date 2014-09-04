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

import static java.text.MessageFormat.format;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.PLUGIN_ID;
import static org.eclipse.recommenders.net.Proxies.proxy;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;

/**
 * Responsible to anonymize (if requested) and send an error report.
 */
public class UploadJob extends Job {

    private Executor executor = Executor.newInstance();
    private ErrorReport event;
    private URI target;
    private Settings settings;

    UploadJob(ErrorReport event, Settings settings, URI target) {
        super(format(Messages.UPLOADJOB_NAME, target));
        this.event = event;
        this.settings = settings;
        this.target = target;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.UPLOADJOB_TASKNAME, 1);
        try {
            String body = ErrorReports.toJson(event, settings, false);
            Request request = Request.Post(target).bodyString(body, ContentType.APPLICATION_JSON);
            Response response = proxy(executor, target).execute(request);
            HttpResponse httpResponse = response.returnResponse();
            String details = EntityUtils.toString(httpResponse.getEntity());
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code >= 400) {
                return new Status(WARNING, PLUGIN_ID, format(Messages.UPLOADJOB_BAD_RESPONSE, details));
            }
            return new Status(IStatus.INFO, PLUGIN_ID, format(Messages.UPLOADJOB_THANK_YOU, details));
        } catch (Exception e) {
            return new Status(WARNING, PLUGIN_ID, Messages.UPLOADJOB_FAILED_WITH_EXCEPTION, e);
        } finally {
            monitor.done();
        }
    }
}
