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

import org.eclipse.recommenders.models.ModelRepository.DownloadCallback;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

public interface IModelRepository {

    /**
     * Returns the file for the given coordinate - if it exists locally. Note that calling this method <b>may</b>
     * trigger a background download of the requested resource depending on the actual implementation.
     */
    Optional<File> getLocation(ModelCoordinate mc);

    /**
     * Resolves the given model coordinate to a local file. If the model does not yet exist locally this method attempts
     * to download the model from the remote repository. This call blocks the caller until the download finished.
     * 
     * @return the path to the locally cached model archive.
     * 
     * @throws Exception
     *             if no model could be downloaded, e.g., because the coordinate does not exist on the remote repository
     *             or a network/io error occurred.
     */
    Optional<File> resolve(ModelCoordinate mc) throws Exception;

    /**
     * Resolves the given model coordinate to a local file. If the model does not yet exist locally this method accesses
     * the remote repository to download it. This call run's in a background process.
     */
    ListenableFuture<File> resolve(ModelCoordinate mc, DownloadCallback callback);

}
