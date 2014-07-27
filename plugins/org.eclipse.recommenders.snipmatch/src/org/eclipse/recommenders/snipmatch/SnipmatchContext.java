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

import static org.eclipse.recommenders.snipmatch.LocationConstraint.NONE;

public class SnipmatchContext implements ISnipmatchContext {

    private final String userQuery;
    private final LocationConstraint locationConstraint;

    public SnipmatchContext(String userQuery, LocationConstraint locationConstraint) {
        this.userQuery = userQuery;
        this.locationConstraint = locationConstraint;
    }

    public SnipmatchContext(String userQuery) {
        this.userQuery = userQuery;
        this.locationConstraint = NONE;
    }

    @Override
    public String getUserQuery() {
        return userQuery;
    }

    @Override
    public LocationConstraint getLocationConstraint() {
        return locationConstraint;
    }

}
