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
package org.eclipse.recommenders.internal.models.rcp;

import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

final class TriggerModelDownloadActionForDependencyInfos extends Action {

    private IProjectCoordinateProvider pcProvider;

    private IModelIndex modelIndex;

    private EclipseModelRepository repo;

    private final Set<DependencyInfo> deps;

    public TriggerModelDownloadActionForDependencyInfos(String text, Set<DependencyInfo> deps,
            IProjectCoordinateProvider pcProvider, IModelIndex modelIndex, EclipseModelRepository repo) {
        super(text);
        this.pcProvider = pcProvider;
        this.modelIndex = modelIndex;
        this.repo = repo;
        this.deps = deps;
    }

    @Override
    public void run() {
        Set<ProjectCoordinate> pcs = Sets.newHashSet();
        for (DependencyInfo dep : deps) {
            Optional<ProjectCoordinate> opc = pcProvider.resolve(dep);
            if (opc.isPresent()) {
                pcs.add(opc.get());
            }
        }
        new TriggerModelDownloadActionForProjectCoordinates(getText(), pcs, modelIndex, repo).run();
    }
}
