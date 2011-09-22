/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.commons.logging.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class LoggingEventManager {

    private static final double REMOVE_RATIO = 0.33;

    static ILoggingEvent[] NONE = new ILoggingEvent[] {};

    private static LoggingEventManager manager;

    private int maxSize = 20000;
    private int removeQuantity = 14000;
    private final List<ILoggingEvent> loggingEventList = Collections.synchronizedList(new ArrayList<ILoggingEvent>());

    private final List<LoggingEventManagerListener> listeners = new ArrayList<LoggingEventManagerListener>();

    private LoggingEventManager() {
    }

    public static LoggingEventManager getManager() {
        if (manager == null) {
            manager = new LoggingEventManager();
        }
        return manager;
    }

    public void addLoggingEvent(final ILoggingEvent event) {
        if (!EventFilter.filter(event)) {
            return;
        }
        loggingEventList.add(event);
        listSizeCheck();
        sendEvent(event);
    }

    private void sendEvent(final ILoggingEvent event) {
        fireLoggingEventAdded(new ILoggingEvent[] { event }, NONE);
    }

    public void addLoggingEventManagerListener(final LoggingEventManagerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeLoggingEventManagerListener(final LoggingEventManagerListener listener) {
        listeners.remove(listener);
    }

    private void fireLoggingEventAdded(final ILoggingEvent[] itemsAdded, final ILoggingEvent[] itemsRemoved) {
        final LoggingEventManagerEvent event = new LoggingEventManagerEvent(this, itemsAdded, itemsRemoved);
        final Iterator<LoggingEventManagerListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().loggingEventsChanged(event);
        }
    }

    private void listSizeCheck() {
        synchronized (loggingEventList) {
            if (loggingEventList.size() > maxSize) {
                final List<ILoggingEvent> sub = loggingEventList.subList(0, removeQuantity);
                sub.clear();
            }
        }
    }

    public void clearEventList() {
        loggingEventList.clear();
    }

    public ILoggingEvent getEvent(final int index) {
        synchronized (loggingEventList) {
            if (index > loggingEventList.size() - 1) {
                return null;
            }
            return loggingEventList.get(index);
        }
    }

    public int getEventCount() {
        return loggingEventList.size();
    }

    public int getIndex(final ILoggingEvent event) {
        return loggingEventList.indexOf(event);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
        final Double dbl = maxSize * REMOVE_RATIO;
        this.removeQuantity = dbl.intValue();
        listSizeCheck();
    }

}
