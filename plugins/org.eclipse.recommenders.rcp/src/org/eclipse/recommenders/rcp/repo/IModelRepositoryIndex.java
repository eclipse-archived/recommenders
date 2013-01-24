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

import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;

public interface IModelRepositoryIndex {

    /**
     * Searches the repository for a model artifact that potentially matches the given model type and jar fingerprint.
     * Please note that the artifact returned is handle only.
     */
    Optional<Artifact> searchByFingerprint(String fingerprint, String modeltype);

    /**
     * Searches the repository for a model artifact that potentially matches the given model type and jar artifact id
     * (XXX we live in a strange world where artifact ids are unique. Hail osgi.). Please note that the artifact
     * returned is only a handle.
     */
    Optional<Artifact> searchByArtifactId(String artifactId, String modeltype);

    void close();

    File getLocation();

    void open();

}
