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

import com.google.common.annotations.Beta;

public class ModelEvents {

    /**
     * Triggered when a model repository url was changed (most lyike in the a preference page).
     * <p>
     * Clients of this event may consider refreshing themselves whenever they receive this event. Clients get notified
     * in a background process.
     */
    @Beta
    public static class ModelRepositoryUrlChangedEvent {
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
    public static class ProjectCoordinateChangeEvent {
        // TODO this event needs to publish the affected IPackageFragmentRoot so that project coordinate providers can
        // clear their cache entry accordingly
    }

    private ModelEvents() {
    }
}
