/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.coordinates;

import java.util.List;

import org.eclipse.recommenders.utils.Result;

import com.google.common.collect.ImmutableList;

/**
 * Registry for managing advisors that suggest {@link ProjectCoordinate}s for {@link DependencyInfo}s.
 */
public interface IProjectCoordinateAdvisorService extends IProjectCoordinateAdvisor {

    /**
     * Returns all advisors this registry is configured with.
     */
    ImmutableList<IProjectCoordinateAdvisor> getAdvisors();

    /**
     * Adds an advisor to the list of currently configured advisors.
     */
    void addAdvisor(IProjectCoordinateAdvisor advisor);

    /**
     * Sets a advisors for this registry. Overwrites any previously configured advisors.
     */
    void setAdvisors(List<IProjectCoordinateAdvisor> advisors);

    Result<ProjectCoordinate> trySuggest(DependencyInfo dependencyInfo);

}
