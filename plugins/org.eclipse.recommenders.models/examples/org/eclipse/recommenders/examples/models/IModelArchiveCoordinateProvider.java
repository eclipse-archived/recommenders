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
package org.eclipse.recommenders.examples.models;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.models.ProjectCoordinate;

import com.google.common.base.Optional;

/**
 * Responsible to find and maintain a mapping from a {@link ProjectCoordinate}
 * to a {@link ModelArchiveCoordinate}. Implementors should be able to persist
 * manually specified mappings and reload them on restart.
 * <p>
 * This interface is not expected to be injected by clients directly.<br>
 * This interface is expected to be extended by model providers.
 */
public interface IModelArchiveCoordinateProvider extends Closeable {

    /**
     * Returns the model-type this provider returns models for, e.g., calls,
     * ovrs etc.
     */
    public String getType();

    /**
     * Framework callback that allows an implementor to perform some IO
     * operations like loading manual mappings from a file etc.
     * 
     * @throws IOException
     */
    public void open() throws IOException;

    /**
     * Returns the best matching {@link ModelArchiveCoordinate} for the given
     * {@link ProjectCoordinate} - if any. This call may lookup the best matches
     * from the {@link ModelRepository}'s model index. If a manual mapping was
     * specified before (using
     * {@link #set(ProjectCoordinate, ModelArchiveCoordinate)}) that coordinate
     * will be returned.
     */
    Optional<ModelArchiveCoordinate> find(ProjectCoordinate pc);

    /**
     * Manually sets the {@link ModelArchiveCoordinate} for the given
     * {@link ProjectCoordinate}. An existing mapping will be overwritten. This
     * mapping is expected to be persisted by the implementor.
     */
    void setOverride(ProjectCoordinate pc, ModelArchiveCoordinate modelId);

    /**
     * Removes a previously found mapping from {@link ProjectCoordinate} to any
     * {@link ModelArchiveCoordinate}. If the mapping was persisted previously,
     * this mapping should still be gone after restart.
     */
    void removeOverride(ProjectCoordinate pc);

    /**
     * Returns wether the mapping for the given {@link ProjectCoordinate} to a
     * {@link ModelArchiveCoordinate} is overridden.
     */
    boolean isOverridden(ProjectCoordinate pc);

}
