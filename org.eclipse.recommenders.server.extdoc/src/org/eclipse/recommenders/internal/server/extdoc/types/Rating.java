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

import java.util.Date;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;

public final class Rating implements IRating {

    private Date date;
    private String user;
    private int rating;

    public static Rating create(final int rating) {
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
    public void validate() {
        Checks.ensureIsTrue(rating > 0);
    }

}
