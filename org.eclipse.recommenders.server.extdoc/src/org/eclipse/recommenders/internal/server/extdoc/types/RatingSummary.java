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
package org.eclipse.recommenders.internal.server.extdoc.types;

import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IRatingSummary;

public final class RatingSummary implements IRatingSummary {

    private int sum;
    private int count;
    private IRating userRating;

    public static IRatingSummary create(final int sum, final int count, final IRating userRating) {
        final RatingSummary summary = new RatingSummary();
        summary.sum = sum;
        summary.count = count;
        summary.userRating = userRating;
        return summary;
    }

    @Override
    public int getAverage() {
        return count == 0 ? 0 : sum / count;
    }

    @Override
    public void addUserRating(final IRating rating) {
        userRating = rating;
        ++count;
        sum += rating.getRating();
    }

    @Override
    public IRating getUserRating() {
        return userRating;
    }

}