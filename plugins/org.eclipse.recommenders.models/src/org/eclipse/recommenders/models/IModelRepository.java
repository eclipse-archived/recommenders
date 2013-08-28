/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.models;

import java.io.File;

import com.google.common.base.Optional;

public interface IModelRepository {

    /**
     * Returns the file for the given model coordinate if it exists locally.
     * 
     * If the caller expects the file to be accessed again in the future, the <code>prefetch</code> should be set.
     * Depending on the implementation, setting the <code>prefetch</code> parameter may result in a background download
     * of the requested file.
     * 
     * Independent of the value of <code>prefetch</code>, this method can be assumed to return quickly.
     */
    Optional<File> getLocation(ModelCoordinate mc, boolean prefetch);

    /**
     * Resolves the given model coordinate to a local file. If the model does not yet exist locally this method may
     * attempt to download the model from the remote repository. If it is absolutely desired that this method attempts a
     * download, the <code>force</code> parameter should be set.
     * 
     * This method blocks the caller until the download (if necessary) is finished; callers must not assume that this
     * method returns quickly.
     * 
     * @param force
     *            ignore previously cached values and retries downloading the coordinate. Does not trigger a download if
     *            the local artifact already exists locally.
     * 
     * @return the path to the locally cached model archive.
     * 
     * @throws Exception
     *             if no file could be downloaded, e.g., because the model coordinate does not exist in the remote
     *             repository or an I/O error has occurred.
     */
    Optional<File> resolve(ModelCoordinate mc, boolean force);

    Optional<File> resolve(ModelCoordinate mc, boolean force, DownloadCallback callback);
}
