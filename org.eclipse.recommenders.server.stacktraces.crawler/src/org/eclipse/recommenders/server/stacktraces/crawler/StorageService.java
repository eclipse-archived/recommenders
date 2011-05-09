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
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.NotFoundException;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;

public class StorageService {

    private final Logger logger = Logger.getLogger(getClass());
    private final ArrayBlockingQueue<StacktraceOccurence> queue;
    private final WebServiceClient dbClient;
    private String escapedQuotation;

    public StorageService(final ClientConfiguration config) {
        this.queue = new ArrayBlockingQueue<StacktraceOccurence>(50);
        new Thread(new QueueConsumer()).start();
        dbClient = new WebServiceClient(config);
        try {
            escapedQuotation = URLEncoder.encode("\"", "UTF-8");
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
