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
package org.eclipse.recommenders.models;

import com.google.common.base.Optional;

/**
 * Resolver for extract the ProjectCoordinate of a classpath dependency element.
 */
public interface IProjectCoordinateAdvisor {

    /**
     * Returns a suggested project coordinate for the given dependency.
     */
    Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo);

}
