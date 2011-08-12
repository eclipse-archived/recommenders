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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IRatingSummary;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedback;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
final class UserFeedback implements IUserFeedback {

    @SerializedName("_id")
    private String documentId;
    @SerializedName("_rev")
    private String rev;

    private String providerId;
    private String element;
    private String item;

    private final Set<Rating> ratings = new HashSet<Rating>();
    private final List<Comment> comments = new LinkedList<Comment>();

    static UserFeedback create(final IProvider provider, final String element, final String item) {
        final UserFeedback feedback = new UserFeedback();
        feedback.providerId = provider.getClass().getSimpleName();
        feedback.element = element;
        feedback.item = item;
        feedback.validate();
        return feedback;
    }

    @Override
    public IRatingSummary getRatingSummary() {
        int sum = 0;
        Rating userRating = null;
        final String userId = UUIDHelper.getUUID();
        for (final Rating rating : ratings) {
            sum += rating.getRating();
            if (rating.getUserId().equals(userId)) {
                userRating = rating;
            }
        }
        return RatingSummary.create(sum, ratings.size(), userRating);
    }

    @Override
    public Collection<? extends IComment> getComments() {
        return comments;
    }

    @Override
    public void addRating(final IRating rating) {
        for (final Rating oldRating : ratings) {
            if (oldRating.getUserId().equals(rating.getUserId())) {
                ratings.remove(oldRating);
            }
        }
        ratings.add((Rating) rating);
    }

    @Override
    public void addComment(final IComment comment) {
        comments.add((Comment) comment);
    }

    @Override
    public String getElementId() {
        return element;
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(!element.isEmpty());
    }

}
