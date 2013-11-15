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
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.models.DependencyInfo.SURROUNDING_PROJECT_FILE;
import static org.eclipse.recommenders.models.DependencyType.PROJECT;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class NestedJarProjectCoordinateAdvisor implements IProjectCoordinateAdvisor {

    @VisibleForTesting
    public IProjectCoordinateProvider pcProvider;

    public Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo) {
        String path = dependencyInfo.getHint(SURROUNDING_PROJECT_FILE).orNull();
        if (StringUtils.isEmpty(path)) {
            return absent();
        }

        File projectFile = new File(path);
        DependencyInfo diForSurroundingProject = new DependencyInfo(projectFile, PROJECT);

        initializePcProvider();
        return pcProvider.resolve(diForSurroundingProject);
    }

    private void initializePcProvider() {
        // not great but if not initializing this lazily I get into circular dependencies when the
        // ProjectCoordinatesView is open.
        if (pcProvider == null) {
            pcProvider = InjectionService.getInstance().requestInstance(IProjectCoordinateProvider.class);
        }
    }
}
