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

import java.util.List;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IRatingSummary;
import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.types.Rating;
import org.eclipse.recommenders.server.extdoc.types.RatingSummary;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.GenericType;

abstract class AbstractRatingsServer implements IStarsRatingsServer {

    private final ICouchDbServer server;

    /**
     * @param server
     *            The CouchDB server to be used for receiving ratings.
     */
    AbstractRatingsServer(final ICouchDbServer server) {
        this.server = server;
    }

    /**
     * @return The CouchDB server in use.
     */
    protected ICouchDbServer getServer() {
        return server;
    }

    @Override
    public IRatingSummary getRatingSummary(final Object object, final IProvider provider) {
        final String providerId = provider.getClass().getSimpleName();
        final String objectId = String.valueOf(object.hashCode());
        final List<RatingSummary> list = server.getRows("stars",
                ImmutableMap.of("providerId", providerId, "object", objectId),
                new GenericType<GenericResultObjectView<RatingSummary>>() {
                });
        if (list == null || list.isEmpty()) {
            return RatingSummary.create(0, 0);
        }
        final RatingSummary summary = list.get(0);
        summary.setUserRating(getUserRating(objectId, provider));
        return summary;
    }

    private Rating getUserRating(final Object object, final IProvider provider) {
        final String providerId = provider.getClass().getSimpleName();
        final String objectId = String.valueOf(object.hashCode());
        final ImmutableMap<String, String> key = ImmutableMap.of("providerId", providerId, "object", objectId, "user",
                UUIDHelper.getUUID());
        final List<Rating> ratings = server.getRows("starsUsers", key,
                new GenericType<GenericResultObjectView<Rating>>() {
                });
        return ratings == null || ratings.isEmpty() ? null : ratings.get(0);
    }

    @Override
    public final IRating addRating(final Object object, final int stars, final IProvider provider) {
        final Rating oldRating = getUserRating(object, provider);
        if (oldRating != null) {
            // TODO: remove old rating
        }

        final IRating rating = Rating.create(provider, object, stars);
        server.post(rating);
        return rating;
    }

}
