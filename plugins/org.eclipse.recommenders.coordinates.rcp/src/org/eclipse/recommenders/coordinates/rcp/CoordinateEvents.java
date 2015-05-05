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
package org.eclipse.recommenders.coordinates.rcp;

import org.eclipse.recommenders.coordinates.DependencyInfo;

import com.google.common.annotations.Beta;

public final class CoordinateEvents {

    private CoordinateEvents() {
        // Not meant to be instantiated
    }

    @Beta
    public static class AdvisorConfigurationChangedEvent {
    }

    @Beta
    public static class ProjectCoordinateChangeEvent {

        public DependencyInfo dependencyInfo;

        public ProjectCoordinateChangeEvent(DependencyInfo dependencyInfo) {
            this.dependencyInfo = dependencyInfo;
        }
    }
}
