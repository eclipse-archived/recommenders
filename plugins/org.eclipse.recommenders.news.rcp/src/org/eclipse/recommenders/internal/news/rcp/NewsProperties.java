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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Singleton;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.recommenders.internal.news.rcp.l10n.LogMessages;
import org.eclipse.recommenders.news.rcp.INewsProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

@Creatable
@Singleton
public class NewsProperties implements INewsProperties {

    private static final String FILENAME_READ_MESSAGES = "read-messages.properties"; //$NON-NLS-1$
    private static final String VALUE_READ = "read"; //$NON-NLS-1$
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
    private final File readMessagesFile;
    private final File pollDatesFile;
    private final File feedDatesFile;

    public NewsProperties() {
        this(getFile(FILENAME_READ_MESSAGES), getFile(Constants.FILENAME_POLL_DATES),
                getFile(Constants.FILENAME_FEED_DATES));
    }

    @VisibleForTesting
    protected NewsProperties(File readMessagesFile, File pollDatesFile, File feedDatesFile) {
        this.readMessagesFile = readMessagesFile;
        this.pollDatesFile = pollDatesFile;
        this.feedDatesFile = feedDatesFile;
    }

    @Override
    public void writeReadIds(Set<String> readIds) {
        Properties properties = new Properties();
        if (readIds == null) {
            return;
        }
        for (String id : readIds) {
            properties.put(id, VALUE_READ);
        }

        try (FileOutputStream stream = new FileOutputStream(readMessagesFile)) {
            properties.store(stream, ""); //$NON-NLS-1$
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_WRITING_PROPERTIES, e, FILENAME_READ_MESSAGES);
        }
    }

    @Override
    public Set<String> getReadIds() {
        if (!readMessagesFile.exists()) {
            return Sets.newConcurrentHashSet();
        }
        Properties properties = new Properties();

        try (InputStream stream = Files.asByteSource(readMessagesFile).openStream()) {

            properties.load(stream);
            return Sets.newConcurrentHashSet(properties.stringPropertyNames());
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_READING_PROPERTIES, e, FILENAME_READ_MESSAGES);
            return Sets.newConcurrentHashSet();
        }
    }

    private static File getFile(String name) {
        Bundle bundle = FrameworkUtil.getBundle(NewsProperties.class);
        File stateLocation = Platform.getStateLocation(bundle).toFile();
        return new File(stateLocation, name);
    }

    @Override
    public Map<String, Date> getDates(String filename) {
        Map<String, Date> result = Maps.newConcurrentMap();
        File file = null;
        if (filename.equals(Constants.FILENAME_FEED_DATES)) {
            file = feedDatesFile;
        } else {
            file = pollDatesFile;
        }
        if (!file.exists()) {
            return result;
        }
        Properties properties = new Properties();
        try (InputStream stream = Files.asByteSource(file).openStream()) {
            properties.load(stream);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                result.put(entry.getKey().toString(), dateFormat.parse((String) entry.getValue()));
            }
            return result;
        } catch (IOException | ParseException e) {
            Logs.log(LogMessages.ERROR_READING_PROPERTIES, e, filename);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * Stores last feed poll date, however it doesn't overwrite it, just add new entries.
     */
    public void writeDates(Map<FeedDescriptor, Date> map, String filename) {
        if (map == null) {
            return;
        }
        Properties properties = new Properties();
        File file = null;
        if (filename.equals(Constants.FILENAME_FEED_DATES)) {
            file = feedDatesFile;
        } else {
            file = pollDatesFile;
        }
        if (file.exists()) {
            try (InputStream stream = Files.asByteSource(file).openStream()) {
                properties.load(stream);
            } catch (IOException e) {
                Logs.log(LogMessages.ERROR_READING_PROPERTIES, e, filename);
            }
        }
        List<String> propertyNames = (List<String>) Collections.list(properties.propertyNames());
        for (Map.Entry<FeedDescriptor, Date> entry : map.entrySet()) {
            if (!propertyNames.contains(entry.getKey().getId())) {
                properties.put(entry.getKey().getId(), dateFormat.format(entry.getValue()));
            } else {
                properties.setProperty(entry.getKey().getId(), dateFormat.format(entry.getValue()));
            }
        }
        try (FileOutputStream stream = new FileOutputStream(file)) {
            properties.store(stream, ""); //$NON-NLS-1$
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_WRITING_PROPERTIES, filename, e);
        }
    }
}
