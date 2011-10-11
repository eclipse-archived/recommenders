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
package org.eclipse.recommenders.rcp.extdoc.feedback;

public interface IRatingSummary {

    /**
     * @return The average rating received from all users.
     */
    int getAverage();

    /**
     * @return The total amount of ratings given by users.
     */
    int getAmountOfRatings();

    void addUserRating(final IRating userRating);

    IRating getUserRating();

}
