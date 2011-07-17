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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedback;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedbackServer;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.UsernamePreferenceListener;
import org.eclipse.recommenders.server.extdoc.types.Comment;
import org.eclipse.recommenders.server.extdoc.types.Rating;
import org.eclipse.recommenders.server.extdoc.types.UserFeedback;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.GenericType;

public abstract class AbstractFeedbackServer implements IUserFeedbackServer {

    private final ICouchDbServer server;
    private final UsernamePreferenceListener listener;
    private final JavaElementResolver resolver;

    public AbstractFeedbackServer(final ICouchDbServer server, final UsernamePreferenceListener usernameListener,
            final JavaElementResolver resolver) {
        this.server = server;
        listener = usernameListener;
        this.resolver = resolver;
    }

    @Override
    public final IUserFeedback getUserFeedback(final IJavaElement javaElement, final IProvider provider) {
        final String providerId = provider.getClass().getSimpleName();
        final IName name = resolveName(javaElement);
        final List<UserFeedback> list = server.getRows("feedback",
                ImmutableMap.of("providerId", providerId, "element", name.getIdentifier()),
                new GenericType<GenericResultObjectView<UserFeedback>>() {
                });
        return list == null || list.isEmpty() ? UserFeedback.create(provider, name) : list.get(0);
    }

    @Override
    public final IRating addRating(final int stars, final IJavaElement javaElement, final IProvider provider) {
        final IUserFeedback feedback = getUserFeedback(javaElement, provider);
        final IRating rating = Rating.create(stars);
        feedback.addRating(rating);
        deleteFeedback(feedback, provider);
        server.post(feedback);
        return rating;
    }

    @Override
    public final IComment addComment(final String text, final IJavaElement javaElement, final IProvider provider) {
        final IUserFeedback feedback = getUserFeedback(javaElement, provider);
        final IComment comment = Comment.create(text, listener.getUsername());
        feedback.addComment(comment);
        deleteFeedback(feedback, provider);
        server.post(feedback);
        return comment;
    }

    /**
     * @return The CouchDB server in use.
     */
    protected final ICouchDbServer getServer() {
        return server;
    }

    private IName resolveName(final IJavaElement javaElement) {
        if (javaElement instanceof IMethod) {
            return resolver.toRecMethod((IMethod) javaElement);
        } else if (javaElement instanceof IType) {
            return resolver.toRecType((IType) javaElement);
        }
        throw new IllegalArgumentException(javaElement.toString());
    }

    private void deleteFeedback(final IUserFeedback feedback, final IProvider provider) {
        if (feedback.getRevision() != null) {
            final String providerId = provider.getClass().getSimpleName();
            server.delete("feedback",
                    ImmutableMap.of("providerId", providerId, "element", feedback.getElement().getIdentifier()),
                    feedback.getRevision());
        }
    }

}
