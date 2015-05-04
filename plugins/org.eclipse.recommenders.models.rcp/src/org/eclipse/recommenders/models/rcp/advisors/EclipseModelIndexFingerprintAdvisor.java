/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models.rcp.advisors;

import javax.inject.Inject;

import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.coordinates.rcp.EclipseProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.advisors.ModelIndexFingerprintAdvisor;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class EclipseModelIndexFingerprintAdvisor implements IProjectCoordinateAdvisor {

    private final ModelIndexFingerprintAdvisor delegate;
    private final EclipseProjectCoordinateAdvisorService advisorService;

    @Inject
    public EclipseModelIndexFingerprintAdvisor(IModelIndex index, EclipseProjectCoordinateAdvisorService advisorService, EventBus bus) {
        delegate = new ModelIndexFingerprintAdvisor(index);
        this.advisorService = advisorService;
        bus.register(this);
    }

    @Subscribe
    public void onEvent(ModelIndexOpenedEvent e) {
        // the fingerprint strategy uses the model index to determine missing project coordinates. Thus we have to
        // invalidate at least all absent values but to be honest, all values need to be refreshed!
        advisorService.clearCache();
    }

    @Override
    public Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo) {
        return delegate.suggest(dependencyInfo);
    }
}
