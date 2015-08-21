/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.news.rcp;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.internal.news.rcp.FeedDescriptor;

public interface INewsProperties {
    /**
     * @return Set of message IDs read by user.
     */
    Set<String> getReadIds();

    /**
     * Persists message IDs read by user.
     *
     * @param readIds
     *            set of messages IDs
     */
    void writeReadIds(Set<String> readIds);

    /**
     * @param filename
     *            name of file to load data from
     * @return Map of String and Date, for example message IDs and date when they were polled
     */
    Map<String, Date> getDates(String filename);

    /**
     *
     * @param map
     *            Map to be saved, for example message IDs and date when they were polled
     * @param filename
     *            name of file to save data to
     */
    void writeDates(Map<FeedDescriptor, Date> map, String filename);
}
