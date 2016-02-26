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
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;

public interface IDownloadService {

    /**
     * Downloads a representation of a Web resource.
     *
     * @param uri
     *            The Web resource to be downloaded
     * @param monitor
     *            A progress monitor, or <code>null</code> if progress reporting and cancellation are not desired
     * @return An <em>unbuffered</em> stream representation of the Web resource
     * @throws IOException
     *             If the resource could not be downloaded
     */
    InputStream download(URI uri, @Nullable IProgressMonitor monitor) throws IOException;

    /**
     * Reads the representation of a previously {@link #download(URI, IProgressMonitor) downloaded} web resource.
     *
     * @param uri
     *            The Web resource
     * @return An <em>unbuffered</em> stream representation of the Web resource or <code>null</code> if the resource has
     *         not yet been {@link #download(URI, IProgressMonitor) downloaded} successfully.
     * @throws IOException
     *             If the representation could not be read
     */
    @Nullable
    InputStream read(URI uri) throws IOException;

    /**
     * @param uri
     *            The Web resource
     * @return The time of the last download attempt or <code>null</code> if no attempt has been made yet
     * @throws IOException
     *             If an I/O error occurs while reading the time
     */
    @Nullable
    Date getLastAttemptDate(URI uri) throws IOException;
}
