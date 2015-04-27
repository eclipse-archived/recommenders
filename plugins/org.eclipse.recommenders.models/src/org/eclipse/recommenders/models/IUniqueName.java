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

import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.utils.names.IName;

/**
 * An {@link IUniqueName} is an identifier qualified with a {@link ProjectCoordinate}. The identifier type can be
 * arbitrary but usually is a sub-type of {@link IName}, hence, the method to obtain the identifier is called
 * {@link #getName()} instead of {@code getIdentifier()}. The {@link ProjectCoordinate} is required to find the right
 * recommendation model for the given identifier. It is in the responsibility of the recommender to qualify the type it
 * wants to make recommendations for. Mapping a {@link ProjectCoordinate} to the actual {@link ModelCoordinate}
 * is done by the {@link IModelProvider}.
 * 
 * @see IModelProvider#acquireModel(IUniqueName)
 */
public interface IUniqueName<T> {

    /**
     * Returns the relative part of this name which must not be <code>null</code>.
     */
    T getName();

    /**
     * Returns the base, i.e., the project coordinate, of this name. The coordinate may default to
     * {@link ProjectCoordinate#UNKNOWN} but must not be <code>null</code>.
     */
    ProjectCoordinate getProjectCoordinate();

    /**
     * {@inheritDoc}
     * 
     * Implementors must implement {@link #equals(Object)} properly. Otherwise model pooling does not work.
     */
    @Override
    boolean equals(Object obj);

    /**
     * {@inheritDoc}
     * 
     * Implementors must implement {@link #hashCode()} properly. Otherwise model pooling does not work.
     */
    @Override
    int hashCode();
}
