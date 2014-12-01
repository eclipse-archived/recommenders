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
import static org.eclipse.recommenders.internal.stacktraces.rcp.ReportState.*;
import static org.eclipse.recommenders.net.Proxies.proxy;

import java.net.URI;

import org.apache.commons.lang3.ArrayUtils;
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IProgressConstants;

import com.google.common.base.Optional;

/**
 * Responsible to anonymize (if requested) and send an error report.
 */
public class UploadJob extends Job {

    private Executor executor;
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
            executor = Executor.newInstance();
            String body = ErrorReports.toJson(event, settings, false);
            Request request = Request.Post(target).bodyString(body, ContentType.APPLICATION_JSON);
            Response response = proxy(executor, target).execute(request);
            HttpResponse httpResponse = response.returnResponse();
            String details = EntityUtils.toString(httpResponse.getEntity());
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code >= 400) {
                return new Status(WARNING, PLUGIN_ID, format(Messages.UPLOADJOB_BAD_RESPONSE, details));
            }
            final ReportState state = GsonUtil.deserialize(details, ReportState.class);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ACTION_PROPERTY, new Action() {
                @Override
                public void run() {
                    Shell activeShell = Display.getCurrent().getActiveShell();
                    new ThankYouDialog(activeShell, state).open();
                }
            });
            Optional<String> info = state.getInformation();
            if (FIXED.equals(state.getResolved().orNull())) {
                String message = format(Messages.UPLOADJOB_ALREADY_FIXED_UPDATE,
                        info.or("The error you reported has been fixed."),
                        state.getBugId().or(Messages.THANKYOUDIALOG_INVALID_SERVER_RESPONSE));
                openPopup(message, state.getBugUrl().orNull());
            } else if (ArrayUtils.contains(state.getKeywords().or(EMPTY_STRINGS), KEYWORD_NEEDINFO)) {
                String message = format(Messages.UPLOADJOB_NEED_FURTHER_INFORMATION, info.or(""),
                        state.getBugId().or(Messages.THANKYOUDIALOG_INVALID_SERVER_RESPONSE));
                openPopup(message, state.getBugUrl().orNull());
            }
            return new Status(IStatus.INFO, PLUGIN_ID, format(Messages.UPLOADJOB_THANK_YOU, details));
        } catch (Exception e) {
            return new Status(WARNING, PLUGIN_ID, Messages.UPLOADJOB_FAILED_WITH_EXCEPTION, e);
        } finally {
            monitor.done();
        }
    }

    private void openPopup(final String message, final String url) {

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Window dialog = new ReportNotificationPopup(message, url);
                dialog.open();
            }
        });
    }
}
