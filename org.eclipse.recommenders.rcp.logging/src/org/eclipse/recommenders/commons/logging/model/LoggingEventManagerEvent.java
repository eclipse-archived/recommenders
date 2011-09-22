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

import java.util.EventObject;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class LoggingEventManagerEvent extends EventObject {

    private static final long serialVersionUID = 1218534891181341451L;

    private final ILoggingEvent[] added;
    private final ILoggingEvent[] removed;

    public LoggingEventManagerEvent(final LoggingEventManager source, final ILoggingEvent[] itemsAdded,
            final ILoggingEvent[] itemsRemoved) {
        super(source);
        added = itemsAdded;
        removed = itemsRemoved;
    }

    public ILoggingEvent[] getItemsAdded() {
        return added;
    }

    public ILoggingEvent[] getItemsRemoved() {
        return removed;
    }
}