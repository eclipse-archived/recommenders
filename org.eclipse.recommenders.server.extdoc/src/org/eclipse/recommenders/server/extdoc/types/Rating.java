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
package org.eclipse.recommenders.server.extdoc.types;

import java.util.Date;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

public final class Rating implements IServerType, IRating {

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private String providerId;

    private String object;
    private Date date;
    private String user;
    private int rating;

    public static Rating create(final IProvider provider, final Object object, final int rating, final String user) {
        final Rating instance = new Rating();
        instance.providerId = provider.getClass().getSimpleName();
        instance.object = String.valueOf(object.hashCode());
        instance.date = new Date();
        instance.user = Preconditions.checkNotNull(user);
        instance.rating = rating;
        instance.validate();
        return instance;
    }

    Date getDate() {
        return date;
    }

    @Override
    public int getRating() {
        return rating;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(rating > 0);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(object).addValue(user).addValue(rating).toString();
    }

}
