/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.logging.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.spi.FilterReply;

public class EventFilter {

    private static ContextBase context = new ContextBase();

    private static List<EvaluatorFilter> filterList = new ArrayList<EvaluatorFilter>();

    public static boolean filter(final ILoggingEvent event) {
        if (filterList.isEmpty()) {
            return true;
        }

        final Iterator<EvaluatorFilter> it = filterList.iterator();
        EvaluatorFilter filter;
        while (it.hasNext()) {
            filter = it.next();
            final FilterReply reply = filter.decide(event);
            if (reply == FilterReply.DENY) {
                return false;
            } else if (reply == FilterReply.ACCEPT) {
                return true;
            }
        }

        return true;
    }

    public static Context getContext() {
        return context;
    }

    public static List<EvaluatorFilter> getAllFilters() {
        return filterList;
    }

    public static void add(final EvaluatorFilter filter) {
        if (!filterList.contains(filter)) {
            filterList.add(filter);
        }
    }

    public static void remove(final EvaluatorFilter filter) {
        if (filterList.contains(filter)) {
            filterList.remove(filter);
        }
    }

    public static void moveFilterUp(final EvaluatorFilter filter) {
        final int index = filterList.indexOf(filter);
        if (index > 0) {
            filterList.remove(filter);
            filterList.add(index - 1, filter);
        }
    }

    public static void moveFilterDown(final EvaluatorFilter filter) {
        final int index = filterList.indexOf(filter);
        if (index < filterList.size() - 1) {
            filterList.remove(filter);
            filterList.add(index + 1, filter);
        }
    }

}
