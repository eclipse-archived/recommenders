/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.news.impl.poll;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.recommenders.news.api.NewsItem;

public interface IFeedItemStore {

    /**
     * @param feedUri
     *            The feed URI for which the RSS news feed is to be stored
     * @param stream
     *            An RSS news feed. Will be closed.
     * @param monitor
     *            A progress monitor, or <code>null</code> if progress reporting and cancellation are not desired
     * @return The list of feed items not stored already
     * @throws IOException
     *             If an I/O error occurs while reading the RSS feed
     */
    List<NewsItem> udpate(URI feedUri, InputStream stream, @Nullable IProgressMonitor monitor) throws IOException;

    List<NewsItem> getNewsItems(URI feedUri);
}
