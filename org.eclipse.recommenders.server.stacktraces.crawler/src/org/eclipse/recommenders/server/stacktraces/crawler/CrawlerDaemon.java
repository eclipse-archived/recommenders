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
package org.eclipse.recommenders.server.stacktraces.crawler;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class CrawlerDaemon implements Runnable {

    private static final int TIME_BEFORE_REVISIT_IN_MS = 1000;

    private final Set<CrawlerConfiguration> configurations;
    private final Injector injector;
    private final StorageService storage;

    @Inject
    public CrawlerDaemon(final Injector injector, final Set<CrawlerConfiguration> configurations,
            final StorageService storage) {
        this.injector = injector;
        this.configurations = configurations;
        this.storage = storage;
    }

    @Override
    public void run() {
        while (true) {
            crawl();
            try {
                Thread.sleep(TIME_BEFORE_REVISIT_IN_MS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void crawl() {
        for (final CrawlerConfiguration configuration : configurations) {
            crawl(configuration);
        }
    }

    private void crawl(final CrawlerConfiguration configuration) {
        final Crawler crawler = injector.getInstance(configuration.type);
        crawler.configure(configuration);
        final Date start = getStartDate(configuration);
        final Date end = getEndDate(start, configuration);
        crawler.crawl(start, end);
    }

    private Date getStartDate(final CrawlerConfiguration configuration) {
        return storage.getLatestEntryFor(configuration.type.getSimpleName(), configuration.nameOfSource);
    }

    private Date getEndDate(final Date start, final CrawlerConfiguration configuration) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(start);
        calendar.add(Calendar.DAY_OF_MONTH, configuration.maximumRetrievalOfDays);
        return calendar.getTime();
    }

}
