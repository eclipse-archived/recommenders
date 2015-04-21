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
package org.eclipse.recommenders.models.rcp;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;

import com.google.common.annotations.Beta;

public final class ModelEvents {

    private ModelEvents() {
        // Not meant to be instantiated
    }

    /**
     * Triggered when a model repository url was changed (most like in the a preference page).
     * <p>
     * Client of this event should be an instance of {@link IModelRepository}. Other clients should have a look at
     * {@link ModelRepositoryClosedEvent} and {@link ModelRepositoryOpenedEvent}. Clients of this event may consider
     * refreshing themselves whenever they receive this event. Clients get notified in a background process.
     */
    @Beta
    public static class ModelRepositoryUrlChangedEvent {
    }

    /**
     * Triggered when the model repository was closed to inform clients that the model repository is currently not
     * available.
     */
    @Beta
    public static class ModelRepositoryClosedEvent {
    }

    /**
     * Triggered when the model repository was opened to inform clients that the model repository is available.
     * <p>
     * Clients of this event may consider refreshing themselves whenever they receive this event. Clients get notified
     * in a background process.
     */
    @Beta
    public static class ModelRepositoryOpenedEvent {
    }

    /**
     * Triggered when a model index was opened. Model repository url changes causes the model index to download the
     * repository's index and open it.
     * <p>
     * Clients of this event may consider refreshing themselves whenever they receive this event. Clients get notified
     * in a background process.
     */
    @Beta
    public static class ModelIndexOpenedEvent {
    }

    @Beta
    public static class AdvisorConfigurationChangedEvent {
    }

    @Beta
    public static class ModelArchiveDownloadedEvent {
        public ModelCoordinate model;

        public ModelArchiveDownloadedEvent(ModelCoordinate model) {
            this.model = model;
        }

        @Override
        public String toString() {
            return model.toString();
        }
    }

    @Beta
    public static class ProjectCoordinateChangeEvent {

        public DependencyInfo dependencyInfo;

        public ProjectCoordinateChangeEvent(DependencyInfo dependencyInfo) {
            this.dependencyInfo = dependencyInfo;
        }
    }
}
