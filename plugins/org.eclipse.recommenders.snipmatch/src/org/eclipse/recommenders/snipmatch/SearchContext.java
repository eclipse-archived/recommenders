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

public class SearchContext implements ISearchContext {

    private final String searchText;
    private final Location location;

    public SearchContext(String searchText, Location location) {
        this.searchText = searchText;
        this.location = location;
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

}
