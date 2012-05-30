/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.repo;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.resolution.DependencyResolutionException;

import com.google.common.base.Optional;

public interface IModelRepository {

    void setRemote(String url);

    /**
     * Resolves the given model coordinate and downloads the corresponding jar from server if not locally available.
     * 
     * @return the local copy of the model artifact
     */
    File resolve(Artifact model, final IProgressMonitor monitor) throws DependencyResolutionException;

    /**
     * Installs the local artifact to the
     */
    void install(Artifact model) throws InstallationException;

    /**
     * Deletes a <b>local</b> artifact from file system.
     */
    void delete(Artifact model);

    /**
     * Checks whether a local artifact (described the given information artifact) is in sync with the remote repository
     */
    boolean isLatest(Artifact model);

    /**
     * Returns the location of the local repository. This is a handle only operation. This file may or may not exists.
     */
    File location(Artifact model);

    Optional<Artifact> findHigestVersion(Artifact model);

    Optional<Artifact> findLowestVersion(Artifact model);

    /**
     * @return the location of this model repository
     */
    File getLocation();

}