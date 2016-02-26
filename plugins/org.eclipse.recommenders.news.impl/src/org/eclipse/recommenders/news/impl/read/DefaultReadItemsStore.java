/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.news.impl.read;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.recommenders.internal.news.impl.l10n.Messages;
import org.eclipse.recommenders.news.api.NewsItem;
import org.eclipse.recommenders.news.api.read.IReadItemsStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogService;

/**
 * File format modeled after <a href="https://www.iana.org/assignments/media-types/text/uri-list">text/uri-list</a>.
 */
public class DefaultReadItemsStore implements IReadItemsStore {

    private final Path fileLocation;

    private final Set<String> readIds;

    @Nullable
    private LogService logService;

    public DefaultReadItemsStore() {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        Path stateLocation = Platform.getStateLocation(bundle).toFile().toPath();
        fileLocation = stateLocation.resolve("read-items.uris"); //$NON-NLS-1$
        readIds = intializeReadIds();
    }

    private Set<String> intializeReadIds() {
        Set<String> readIds = new ConcurrentSkipListSet<>();

        if (Files.exists(fileLocation)) {
            try (BufferedReader in = Files.newBufferedReader(fileLocation, StandardCharsets.UTF_8)) {
                String line;
                while ((line = in.readLine()) != null) {
                    String id = line.substring(0, line.length());
                    readIds.add(id);
                }
            } catch (IOException e) {
                logError(Messages.LOG_ERROR_FAILED_TO_INITIALIZE_READ_ITEMS, e);
            }
        }

        return readIds;
    }

    @Override
    public void markAsRead(NewsItem feedItem) {
        String id = feedItem.getId();
        if (readIds.add(id)) {
            synchronized (fileLocation) {
                try (Writer out = Files.newBufferedWriter(fileLocation, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    out.append(id).append('\r').append('\n');
                } catch (IOException e) {
                    logError(Messages.LOG_ERROR_FAILED_TO_MARKED_AS_READ, e);
                }
            }
        }
    }

    @Override
    public boolean isRead(NewsItem feedItem) {
        return readIds.contains(feedItem.getId());
    }

    public synchronized void bindLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

    private void logError(String message, Throwable e) {
        LogService localLogService = this.logService;
        if (localLogService != null) {
            localLogService.log(LogService.LOG_ERROR, message, e);
        }
    }
}
