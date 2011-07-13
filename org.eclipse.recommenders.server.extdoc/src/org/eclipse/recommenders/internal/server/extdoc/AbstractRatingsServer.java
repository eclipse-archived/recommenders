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
import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.types.Rating;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.GenericType;

abstract class AbstractRatingsServer implements IStarsRatingsServer {

    private final ICouchDbServer server;

    AbstractRatingsServer(final ICouchDbServer server) {
        this.server = server;
    }

    protected ICouchDbServer getServer() {
        return server;
    }

    @Override
    public final int getAverageRating(final Object object, final IProvider provider) {
        final String providerId = provider.getClass().getSimpleName();
        final String objectId = String.valueOf(object.hashCode());
        final List<RatingSummary> list = server.getRows("stars",
                ImmutableMap.of("providerId", providerId, "object", objectId),
                new GenericType<GenericResultObjectView<RatingSummary>>() {
                });
        return list.isEmpty() ? 0 : list.get(0).getAverage();
    }

    @Override
    public final IRating getUserRating(final Object object, final IProvider provider) {
        final String providerId = provider.getClass().getSimpleName();
        final String objectId = String.valueOf(object.hashCode());
        final ImmutableMap<String, String> key = ImmutableMap.of("providerId", providerId, "object", objectId, "user",
                UUIDHelper.getUUID());
        final List<Rating> rating = server.getRows("starsUsers", key,
                new GenericType<GenericResultObjectView<Rating>>() {
                });
        return rating.isEmpty() ? null : rating.get(0);
    }

    @Override
    public final void addRating(final Object object, final int stars, final IProvider provider) {
        final IRating oldRating = getUserRating(object, provider);
        if (oldRating != null) {
            // TODO: remove old rating
        }

        final Rating rating = Rating.create(provider, object, stars);
        server.post(rating);
    }

    private static final class RatingSummary {
        private int sum;
        private int count;

        private int getAverage() {
            return count == 0 ? 0 : sum / count;
        }
    }

}
