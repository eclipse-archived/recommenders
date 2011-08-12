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
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedback;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedbackServer;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.UsernameProvider;

import com.sun.jersey.api.client.GenericType;

/**
 * Abstract superclass for servers which want to store user feedback expressed
 * through a provider.
 */
public abstract class AbstractFeedbackServer implements IUserFeedbackServer {

    private final ICouchDbServer server;
    private final UsernameProvider username;

    /**
     * @param server
     *            The server implementation used to receive and store feedback.
     *            Can be accessed by subclasses.
     * @param usernameProvider
     *            Provides the user's name from a preference page. Should be
     *            used together with a IPropertyChangeListener.
     */
    protected AbstractFeedbackServer(final ICouchDbServer server, final UsernameProvider usernameProvider) {
        this.server = server;
        username = usernameProvider;
    }

    @Override
    public final IUserFeedback getUserFeedback(final IName javaElement, final String keyAppendix,
            final IProvider provider) {
        final String providerId = provider.getClass().getSimpleName();
        final String elementId = javaElement.getIdentifier();
        final Map<String, String> key = new HashMap<String, String>();
        key.put("providerId", providerId);
        key.put("element", elementId);
        if (keyAppendix != null) {
            key.put("item", keyAppendix);
        }
        final List<UserFeedback> feedbacks = server.getRows("feedback", key,
                new GenericType<GenericResultObjectView<UserFeedback>>() {
                });
        return feedbacks == null || feedbacks.isEmpty() ? UserFeedback.create(provider, elementId, keyAppendix)
                : feedbacks.get(0);
    }

    @Override
    public final IRating addRating(final int stars, final IName javaElement, final String keyAppendix,
            final IProvider provider) {
        final IUserFeedback feedback = getUserFeedback(javaElement, keyAppendix, provider);
        final IRating rating = Rating.create(stars);
        feedback.addRating(rating);
        storeFeedback(feedback);
        return rating;
    }

    @Override
    public final IComment addComment(final String text, final IName javaElement, final String keyAppendix,
            final IProvider provider) {
        final IUserFeedback feedback = getUserFeedback(javaElement, keyAppendix, provider);
        final IComment comment = Comment.create(text, username.getUsername());
        feedback.addComment(comment);
        storeFeedback(feedback);
        return comment;
    }

    /**
     * @return The CouchDB server in use.
     */
    protected final ICouchDbServer getServer() {
        return server;
    }

    /**
     * @param feedback
     *            Feedback to be either newly stored or updated.
     */
    private void storeFeedback(final IUserFeedback feedback) {
        if (feedback.getDocumentId() == null) {
            server.post(feedback);
        } else {
            server.put(feedback.getDocumentId(), feedback);
        }
    }

}
