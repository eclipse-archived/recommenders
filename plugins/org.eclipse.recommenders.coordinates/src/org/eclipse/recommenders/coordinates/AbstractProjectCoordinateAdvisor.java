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
package org.eclipse.recommenders.coordinates;

import static com.google.common.base.Optional.absent;

import com.google.common.base.Optional;

public abstract class AbstractProjectCoordinateAdvisor implements IProjectCoordinateAdvisor {

    @Override
    public Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo) {
        if (!isApplicable(dependencyInfo.getType())) {
            return absent();
        }
        return doSuggest(dependencyInfo);
    }

    protected abstract boolean isApplicable(DependencyType type);

    protected abstract Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo);

}
