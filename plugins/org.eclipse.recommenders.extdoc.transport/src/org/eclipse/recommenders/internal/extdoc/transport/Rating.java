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
package org.eclipse.recommenders.internal.extdoc.transport;

import java.util.Date;

import org.eclipse.recommenders.extdoc.rcp.feedback.IRating;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;
import org.eclipse.recommenders.utils.Checks;

final class Rating implements IRating {

    private Date date;
    private String user;
    private int rating;

    static Rating create(final int rating) {
        final Rating instance = new Rating();
        instance.date = new Date();
        instance.user = UUIDHelper.getUUID();
        instance.rating = rating;
        instance.validate();
        return instance;
    }

    @Override
    public int getRating() {
        return rating;
    }

    @Override
    public String getUserId() {
        return user;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(rating > 0 && rating <= 5);
    }

}
