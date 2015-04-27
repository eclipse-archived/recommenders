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

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ProjectCoordinateAdvisorService implements IProjectCoordinateAdvisorService {

    private List<IProjectCoordinateAdvisor> advisors = Lists.newArrayList();

    @Override
    public ImmutableList<IProjectCoordinateAdvisor> getAdvisors() {
        return ImmutableList.copyOf(advisors);
    }

    @Override
    public void addAdvisor(IProjectCoordinateAdvisor strategy) {
        advisors.add(strategy);
    }

    @Override
    public void setAdvisors(List<IProjectCoordinateAdvisor> strategies) {
        advisors = strategies;
    }

    /**
     * Iterates over all registered {@link IProjectCoordinateAdvisor}s and returns the result of the first successful
     * advisor.
     */
    @Override
    public Optional<ProjectCoordinate> suggest(final DependencyInfo dependencyInfo) {
        for (IProjectCoordinateAdvisor a : advisors) {
            Optional<ProjectCoordinate> optionalProjectCoordinate = a.suggest(dependencyInfo);
            if (optionalProjectCoordinate.isPresent()) {
                return optionalProjectCoordinate;
            }
        }
        return absent();
    }
}
