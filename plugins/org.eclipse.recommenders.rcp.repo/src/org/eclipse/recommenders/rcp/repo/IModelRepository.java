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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.resolution.DependencyResolutionException;

import com.google.common.base.Optional;

public interface IModelRepository {

    void setRemote(String url);

    void deploy(Artifact artifact, IProgressMonitor monitor) throws DeploymentException;

    /**
     * Resolves and downloads all dependencies for a given artifact.
     * 
     * @return a list of dependencies required by the given artifact - including the artifact itself (probably)
     */
    List<File> resolve(Artifact artifact, final IProgressMonitor monitor) throws DependencyResolutionException;

    /**
     * Installs the local artifact to the
     */
    void install(Artifact artifact) throws InstallationException;

    /**
     * Deletes a <b>local</b> artifact from file system.
     */
    void delete(Artifact artifact);

    /**
     * Checks whether a local artifact (described the given information artifact) is in sync with the remote repository
     */
    boolean isLatest(Artifact artifact);

    /**
     * Returns the location of the local repository. This is a handle only operation. This file may or may not exists.
     */
    File location(Artifact artifact);

    Optional<Artifact> findHigestVersion(Artifact artifact);

    Optional<Artifact> findLowestVersion(Artifact artifact);

}