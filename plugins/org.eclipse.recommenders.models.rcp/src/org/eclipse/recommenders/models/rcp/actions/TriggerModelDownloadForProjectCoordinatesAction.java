/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - Move to new file.
 */
package org.eclipse.recommenders.models.rcp.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.models.rcp.EclipseModelRepository;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelCoordinate;

import com.google.common.eventbus.EventBus;

public class TriggerModelDownloadForProjectCoordinatesAction extends TriggerModelDownloadForModelCoordinatesAction {

    private final List<String> modelTypes;

    private IModelIndex modelIndex;

    private Set<ProjectCoordinate> pcs = new HashSet<>();

    TriggerModelDownloadForProjectCoordinatesAction(String text, List<String> modelClassifier, IModelIndex modelIndex, EclipseModelRepository repo,
            EventBus bus) {
        super(text, repo, bus);
        this.modelTypes = modelClassifier;
        this.modelIndex = modelIndex;
    }

    TriggerModelDownloadForProjectCoordinatesAction(String text, Set<ProjectCoordinate> pcs, List<String> modelClassifier, IModelIndex modelIndex,
            EclipseModelRepository repo, EventBus bus) {
        this(text, modelClassifier, modelIndex, repo, bus);
        this.pcs = pcs;
    }

    @Override
    public void run() {
        triggerDownloadForProjectCoordinates(pcs);
    }

    public void triggerDownloadForProjectCoordinates(Set<ProjectCoordinate> pcs) {
        Set<ModelCoordinate> mcs = new HashSet<>();
        for (ProjectCoordinate pc : pcs) {
            for (String modelType : modelTypes) {
                ModelCoordinate mc = modelIndex.suggest(pc, modelType).orNull();
                if (mc != null) {
                    mcs.add(mc);
                }
            }
        }
        triggerDownloadForModelCoordinates(mcs);
    }
}
