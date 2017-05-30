/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import java.util.Set;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;

public interface ISearchContext {

    String getSearchText();

    Location getLocation();

    boolean isRestrictedByFilename();

    String getFilename();

    boolean isRestrictedByDependencies();

    Set<ProjectCoordinate> getDependencies();
}
