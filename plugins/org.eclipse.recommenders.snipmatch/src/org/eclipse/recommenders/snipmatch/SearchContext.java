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

import static java.util.Objects.requireNonNull;
import static org.eclipse.recommenders.snipmatch.Location.NONE;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;

public class SearchContext implements ISearchContext {

    private final String searchText;
    private final Location location;
    private final String filename;
    private final Set<ProjectCoordinate> availableDependencies;

    public SearchContext(String searchText, Location location, String filename,
            Set<ProjectCoordinate> availableDependencies) {
        this.searchText = requireNonNull(searchText);
        this.location = requireNonNull(location);
        this.filename = requireNonNull(filename);
        this.availableDependencies = requireNonNull(availableDependencies);
    }

    public SearchContext(String searchText) {
        this.searchText = requireNonNull(searchText);
        this.location = NONE;
        this.filename = null;
        this.availableDependencies = null;
    }

    @Override
    public String getSearchText() {
        return searchText;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public boolean isRestrictedByDependencies() {
        return availableDependencies != null;
    }

    @Override
    public Set<ProjectCoordinate> getDependencies() {
        return availableDependencies;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
