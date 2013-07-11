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
package org.eclipse.recommenders.models.dependencies;

import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.models.ProjectCoordinate;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

public class DependencyIdentificationService {

    /**
     * @client preference page for organizing project coordinate resolvers (enable, disable)
     */
    public interface ProjectCoordinateResolverConfigurationService {

        List<IProjectCoordinateResolver> getStrategies();

        void setStrategies(List<IProjectCoordinateResolver> s);

        void addStrategies(IProjectCoordinateResolver s);

        void removeStrategies(IProjectCoordinateResolver s);

    }

    /**
     * @client Dependency monitor view. Needs to know which dependencies exist in the current IDE workspace.
     */
    public interface DependecyInfoStateService {
        /**
         * Immutable list but elements can be modified
         */
        List<DependencyInfo> getDependencies();
    }

    /**
     * @client Completion Engines,
     */
    public interface DependencyInfoLookupService {
        // only internal by event bus...
        // @subscribe methods

        /**
         * Fast lookup, users should not cache the return value for longer than the current action! Project coordinates
         * may change over time.
         **/
         Optional<ProjectCoordinate> get(final DependencyInfo info);
    }

    Map<DependencyInfo, DependencyResolutionStatus> resolutionState = Maps.newConcurrentMap();

    @Subscribe
    void internal_onNewClassPathDependecyFoundEvent() {
        //
        // added neue einträge in "infos"
        // fire events für new infos
    }

    @Subscribe
    void internal_onClassPathDependencyRemovedEvent() {
        // fire remove info element
    }

}
