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

import java.io.IOException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public interface IModelIndex extends IModelArchiveCoordinateAdvisor {

    ModelCoordinate INDEX = new ModelCoordinate("org.eclipse.recommenders", "index", null, "zip", "0.0.0");

    @Override
    void open() throws IOException;

    @Override
    void close() throws IOException;

    ImmutableSet<ModelCoordinate> getKnownModels(String modelType);

    // TODO review whether this should take a version or return a set of pcs (vaadin example 7.1.1 vs 7.0.0-SNAPSHOT)
    Optional<ProjectCoordinate> suggestProjectCoordinateByArtifactId(String artifactId);

    Optional<ProjectCoordinate> suggestProjectCoordinateByFingerprint(String fingerprint);

}
