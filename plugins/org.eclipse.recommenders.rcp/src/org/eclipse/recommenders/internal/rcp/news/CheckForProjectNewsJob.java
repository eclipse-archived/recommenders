/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.news;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.utils.Urls.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.recommenders.internal.rcp.Constants;
import org.eclipse.recommenders.internal.rcp.LogMessages;
import org.eclipse.recommenders.net.Proxies;
import org.eclipse.recommenders.rcp.utils.Shells;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Pair;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;

public class CheckForProjectNewsJob extends Job {

    static final String NEWS_ENABLED = "news-enabled";
    static final String NEWS_LAST_CHECK = "news-last-check";

    static IEclipsePreferences getPreferences() {
        return InstanceScope.INSTANCE.getNode(Constants.BUNDLE_ID);
    }

    // Code Recommenders project feed:
    private URI feed = toUri(toUrl("http://www.codetrails.com/blog/feed/planet-eclipse"));
    private IEclipsePreferences prefs;

    public CheckForProjectNewsJob() {
        super("Loading Project Newsfeed...");
        setSystem(true);
        prefs = getPreferences();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            doRun();
        } catch (Exception e) {
            Logs.log(LogMessages.LOG_WARNING_EXCEPTION_PARSING_NEWS_FEED, e);
        }
        return Status.OK_STATUS;
    }

    @Override
    public boolean shouldRun() {
        // if user disabled - do not run
        boolean enabled = prefs.getBoolean(NEWS_ENABLED, true);
        if (!enabled) {
            return false;
        }
        // if last run is less than 4 days in the past - do not run
        long time = prefs.getLong(NEWS_LAST_CHECK, 0);
        Date last = new Date(time);
        Date now = new Date();
        Date pause = DateUtils.addDays(last, 4);
        if (pause.after(now)) {
            return false;
        }
        // let's check.
        return true;
    }

    private void doRun() throws IOException {
        String string = download().orNull();
        if (string == null) {
            return;
        }
        Date last = getLastRun();
        List<Pair<String, URL>> entries = RssParser.getEntries(string, last);
        saveLastRun(new Date());
        if (entries.isEmpty()) {
            return;
        }
        final Pair<String, URL> latest = entries.get(0);
        final String link = format(
                "The Code Recommenders project has published a new blog post: {0}. <a href=\"{1}\">Read more...</a>",
                latest.getFirst(), latest.getSecond());

        Shells.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                new NewsNotificationPopup(link).open();
            }
        });
    }

    private void saveLastRun(Date last) {
        prefs.putLong(NEWS_LAST_CHECK, last.getTime());
    }

    private Date getLastRun() {
        long time = prefs.getLong(NEWS_LAST_CHECK, System.currentTimeMillis());
        Date last = new Date(time);
        return last;
    }

    private Optional<String> download() throws IOException {
        Executor executor = Executor.newInstance();
        Request request = Request.Get(feed);
        Response response = Proxies.proxy(executor, feed).execute(request);
        HttpResponse httpResponse = response.returnResponse();
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
            return Optional.absent();
        }
        HttpEntity entity = httpResponse.getEntity();
        return Optional.of(IOUtils.toString(entity.getContent(), Charsets.UTF_8.name()));
    }
}
