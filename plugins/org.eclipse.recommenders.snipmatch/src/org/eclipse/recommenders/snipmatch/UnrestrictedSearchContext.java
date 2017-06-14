/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import java.util.Collections;
import java.util.Set;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;

public class UnrestrictedSearchContext implements ISearchContext {

    private final String searchText;

    public UnrestrictedSearchContext(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public String getSearchText() {
        return searchText;
    }

    @Override
    public Location getLocation() {
        return Location.NONE;
    }

    @Override
    public boolean isRestrictedByFilename() {
        return false;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public boolean isRestrictedByDependencies() {
        return false;
    }

    @Override
    public Set<ProjectCoordinate> getDependencies() {
        return Collections.emptySet();
    }
}
