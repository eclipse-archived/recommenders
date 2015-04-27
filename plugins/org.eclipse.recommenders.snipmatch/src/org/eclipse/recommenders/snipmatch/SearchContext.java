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

import static org.eclipse.recommenders.snipmatch.Location.NONE;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;

import com.google.common.collect.Sets;

public class SearchContext implements ISearchContext {

    private final String searchText;
    private final Location location;
    private final Set<ProjectCoordinate> pcs;

    public SearchContext(String searchText, Location location, Set<ProjectCoordinate> pcs) {
        this.searchText = searchText;
        this.location = location;
        this.pcs = pcs;
    }

    public SearchContext(String searchText, Location location) {
        this(searchText, location, Sets.<ProjectCoordinate>newHashSet());
    }

    public SearchContext(String searchText) {
        this(searchText, NONE);
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
    public Set<ProjectCoordinate> getDependencies() {
        return pcs;
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
