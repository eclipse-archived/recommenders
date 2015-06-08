/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.internal.news.rcp.l10n.LogMessages;
import org.eclipse.recommenders.news.rcp.INewsFeedProperties;
import org.eclipse.recommenders.utils.Logs;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class NewsFeedProperties implements INewsFeedProperties {

    private static final String FILENAME = "read-messages.properties";

    private static final String VALUE_READ = "read";

    @Override
    public void writeReadIds(Set<String> readIds) {
        Properties properties = new Properties();
        for (String id : readIds) {
            properties.put(id, VALUE_READ);
        }

        try (FileOutputStream stream = new FileOutputStream(getFile())) {
            properties.store(stream, "");
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_WRITING_PROPERTIES);
        }
    }

    @Override
    public Set<String> getReadIds() {
        File statusFile = getFile();
        if (!statusFile.exists()) {
            return Sets.newConcurrentHashSet();
        }
        Properties properties = new Properties();

        try (InputStream stream = Files.asByteSource(statusFile).openStream()) {

            properties.load(stream);
            return Sets.newConcurrentHashSet(properties.stringPropertyNames());
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_READING_PROPERTIES);
            return Sets.newConcurrentHashSet();
        }
    }

    private static File getFile() {
        Bundle bundle = FrameworkUtil.getBundle(NewsFeedProperties.class);
        File stateLocation = Platform.getStateLocation(bundle).toFile();
        return new File(stateLocation, FILENAME);

    }

    @Override
    public Map<FeedDescriptor, Date> getPollDates() {
        // TODO doing similar job to getReadIds(), but returns poll dates
        return null;
    }

    @Override
    public void writePollDates(Map<FeedDescriptor, Date> map) {
        // TODO doing similar job to writeReadIds(), but store poll dates

    }

    private String mapToString(Map<FeedDescriptor, Date> map) {
        return null;
    }

    private Map<FeedDescriptor, Date> stringToMap(String s) {
        return null;
    }

}
