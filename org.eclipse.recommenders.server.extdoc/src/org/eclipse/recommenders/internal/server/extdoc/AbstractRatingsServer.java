/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;

public abstract class AbstractRatingsServer implements IStarsRatingsServer {

    private final Map<Object, Integer> userRatings = new HashMap<Object, Integer>();

    private final Map<Object, Integer> starsCounts = new HashMap<Object, Integer>();
    private final Map<Object, Integer> starsSums = new HashMap<Object, Integer>();

    @Override
    public final int getAverageRating(final Object object) {
        if (!starsCounts.containsKey(object)) {
            final Integer count = (int) Math.ceil(Math.random() * 3.0);
            starsCounts.put(object, count);
            starsSums.put(object, (int) (Math.ceil(Math.random() * 5.0) * count));
        }
        return starsSums.get(object) / starsCounts.get(object);
    }

    @Override
    public final int getUserRating(final Object object) {
        return userRatings.containsKey(object) ? userRatings.get(object) : -1;
    }

    @Override
    public final void addRating(final Object object, final int stars) {
        userRatings.put(object, stars);
        starsCounts.put(object, starsCounts.get(object) + 1);
        starsSums.put(object, starsSums.get(object) + stars);
    }

}
