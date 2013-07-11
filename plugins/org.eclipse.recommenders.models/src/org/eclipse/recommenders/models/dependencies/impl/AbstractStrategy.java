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
package org.eclipse.recommenders.models.dependencies.impl;

import static com.google.common.base.Optional.absent;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.IProjectCoordinateResolver;

import com.google.common.base.Optional;

public abstract class AbstractStrategy implements IProjectCoordinateResolver {

    @Override
    public Optional<ProjectCoordinate> searchForProjectCoordinate(DependencyInfo dependencyInfo) {
        if (!isApplicable(dependencyInfo.getType())) {
            return absent();
        }
        return extractProjectCoordinateInternal(dependencyInfo);
    }

    protected abstract Optional<ProjectCoordinate> extractProjectCoordinateInternal(DependencyInfo dependencyInfo);

}
