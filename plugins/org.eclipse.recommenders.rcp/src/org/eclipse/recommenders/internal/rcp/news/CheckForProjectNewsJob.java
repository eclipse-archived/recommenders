/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.news;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.internal.rcp.Constants.*;
import static org.eclipse.recommenders.internal.rcp.Messages.*;
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
import org.eclipse.recommenders.internal.rcp.LogMessages;
import org.eclipse.recommenders.net.Proxies;
import org.eclipse.recommenders.rcp.utils.Shells;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.Pair;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class CheckForProjectNewsJob extends Job {

    // Code Recommenders project feed:
    private URI feed = toUri(toUrl("http://www.codetrails.com/blog/feed/planet-eclipse")); //$NON-NLS-1$
    private IEclipsePreferences prefs;

    public CheckForProjectNewsJob(IEclipsePreferences prefs) {
        super(NEWS_LOADING_MESSAGE);
        setSystem(true);
        this.prefs = prefs;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            doRun();
        } catch (Exception e) {
            Logs.log(LogMessages.WARNING_EXCEPTION_PARSING_NEWS_FEED, e);
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
        List<Pair<String, URL>> newsItems = getNewsItems(getRSSFeed().orNull());
        String link = createNotificationLink(newsItems);
        openPopup(link);
    }

    @VisibleForTesting
    protected List<Pair<String, URL>> getNewsItems(@Nullable String rssFeed) {
        Date last = getLastRun();
        List<Pair<String, URL>> newsItems = RssParser.getEntries(rssFeed, last);
        saveLastRun();
        return newsItems;
    }

    @VisibleForTesting
    protected String createNotificationLink(List<Pair<String, URL>> entries) {
        if (entries.isEmpty()) {
            return ""; //$NON-NLS-1$
        }

        Pair<String, URL> latest = entries.get(0);
        String link = format(NEWS_NOTIFY_MESSAGE, latest.getFirst(), latest.getSecond());
        return link;
    }

    private void openPopup(@Nullable final String link) {
        if (Strings.isNullOrEmpty(link)) {
            return;
        }

        Shells.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                new NewsNotificationPopup(link).open();
            }
        });
    }

    private void saveLastRun() {
        prefs.putLong(NEWS_LAST_CHECK, new Date().getTime());
    }

    private Date getLastRun() {
        long time = prefs.getLong(NEWS_LAST_CHECK, System.currentTimeMillis());
        return new Date(time);
    }

    private Optional<String> getRSSFeed() throws IOException {
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
