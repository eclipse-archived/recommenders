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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.NotFoundException;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;

import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public class StorageService {

    private final Logger logger = Logger.getLogger(getClass());
    private final ArrayBlockingQueue<StacktraceOccurence> queue;
    private final WebServiceClient dbClient;
    private String escapedQuotation;
    private String emptyObject;

    @Inject
    public StorageService(final ClientConfiguration config) {
        this.queue = new ArrayBlockingQueue<StacktraceOccurence>(50);
        new Thread(new QueueConsumer()).start();
        dbClient = new WebServiceClient(config);
        try {
            escapedQuotation = URLEncoder.encode("\"", "UTF-8");
            emptyObject = URLEncoder.encode("{}", "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void store(final StacktraceOccurence occurence) {
        try {
            queue.put(occurence);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void storeInDatabase(final StacktraceOccurence occurence) {
        logger.info("Storing occurence to database. Queue state: (" + queue.size() + "/"
                + (queue.size() + queue.remainingCapacity()) + ")");

        final String encodedId = encode(occurence.id);

        try {
            final StacktraceOccurence oldOccurence = dbClient.doGetRequest(encodedId, StacktraceOccurence.class);
            logger.info("Update document with id: " + occurence.id);
            occurence.rev = oldOccurence.rev;
            dbClient.doPutRequest(encodedId, occurence, TransactionResult.class);
        } catch (final NotFoundException e) {
            dbClient.doPostRequest("", occurence);
        }
    }

    public Date getLatestEntryFor(final String type, final String source) {
        final GenericResultObjectView<StacktraceOccurence> result = dbClient.doGetRequest(String.format(
                "_design/occurences/_view/byTypeAndSource?descending=true&limit=1&startkey=[%s%s%s,%s%s%s,%s]",
                escapedQuotation, encode(type), escapedQuotation, escapedQuotation, encode(source), escapedQuotation,
                emptyObject), new GenericType<GenericResultObjectView<StacktraceOccurence>>() {
        });
        if (result.rows.size() != 1) {
            return createDefaultStartDate();
        } else {
            return result.rows.get(0).value.lastModification;
        }
    }

    private Date createDefaultStartDate() {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(0, 0, 0);
        return calendar.getTime();
    }

    public void shutdown() {
        try {
            queue.put(new StopConsumingEvent());
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private class QueueConsumer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    final StacktraceOccurence occurence = queue.take();
                    if (occurence instanceof StopConsumingEvent) {
                        return;
                    }

                    storeInDatabase(occurence);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static class StopConsumingEvent extends StacktraceOccurence {

    }

}
