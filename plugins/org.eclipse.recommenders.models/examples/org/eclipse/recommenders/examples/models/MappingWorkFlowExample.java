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
package org.eclipse.recommenders.examples.models;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.IMappingProvider;
import org.eclipse.recommenders.models.dependencies.IProjectCoordinateResolver;
import org.eclipse.recommenders.models.dependencies.impl.MavenPomPropertiesStrategy;

import com.google.common.base.Optional;

public class MappingWorkFlowExample {

    public static void useOfMapping(final IMappingProvider mapping) {
        DependencyInfo ed = null;

        mapping.addStrategy(new MavenPomPropertiesStrategy());

        IProjectCoordinateResolver mappingStrategy = mapping;

        Optional<ProjectCoordinate> optionalProjectCoordinate = mappingStrategy.searchForProjectCoordinate(ed);

        ProjectCoordinate projectCoordinate = null;
        if (optionalProjectCoordinate.isPresent()) {
            projectCoordinate = optionalProjectCoordinate.get();
        }
    }

}
