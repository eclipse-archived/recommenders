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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.internal.server.extdoc.types.Comment;
import org.eclipse.recommenders.internal.server.extdoc.types.Rating;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedback;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedbackServer;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.UsernameProvider;
import org.eclipse.recommenders.server.extdoc.types.UserFeedback;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.GenericType;

/**
 * Abstract superclass for servers which want to store user feedback expressed
 * through a provider.
 */
public abstract class AbstractFeedbackServer implements IUserFeedbackServer {

    private final ICouchDbServer server;
    private final UsernameProvider username;
    private final JavaElementResolver resolver;

    /**
     * @param server
     *            The server implementation used to receive and store feedback.
     *            Can be accessed by subclasses.
     * @param usernameProvider
     *            Provides the user's name from a preference page. Should be
     *            used together with a IPropertyChangeListener.
     * @param resolver
     *            Used to resolve {@link IJavaElement}s to generate server IDs.
     */
    protected AbstractFeedbackServer(final ICouchDbServer server, final UsernameProvider usernameProvider,
            final JavaElementResolver resolver) {
        this.server = server;
        username = usernameProvider;
        this.resolver = resolver;
    }

    @Override
    public final IUserFeedback getUserFeedback(final IJavaElement javaElement, final IProvider provider) {
        final String providerId = provider.getClass().getSimpleName();
        final String elementId = resolveNameIdentifier(javaElement);
        final List<UserFeedback> feedbacks = server.getRows("feedback",
                ImmutableMap.of("providerId", providerId, "element", elementId),
                new GenericType<GenericResultObjectView<UserFeedback>>() {
                });
        return feedbacks == null || feedbacks.isEmpty() ? UserFeedback.create(provider, elementId) : feedbacks.get(0);
    }

    @Override
    public final IRating addRating(final int stars, final IJavaElement javaElement, final IProvider provider) {
        final IUserFeedback feedback = getUserFeedback(javaElement, provider);
        final IRating rating = Rating.create(stars);
        feedback.addRating(rating);
        storeFeedback(feedback);
        return rating;
    }

    @Override
    public final IComment addComment(final String text, final IJavaElement javaElement, final IProvider provider) {
        final IUserFeedback feedback = getUserFeedback(javaElement, provider);
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
     * @param javaElement
     *            The element to be resolved to an ID.
     * @return An ID string to be used as the element's identifier in the
     *         database.
     */
    private String resolveNameIdentifier(final IJavaElement javaElement) {
        if (javaElement instanceof IMethod) {
            return resolver.toRecMethod((IMethod) javaElement).getIdentifier();
        } else if (javaElement instanceof IType) {
            return resolver.toRecType((IType) javaElement).getIdentifier();
        } else if (javaElement instanceof ILocalVariable) {
            return resolver.toRecMethod((IMethod) javaElement.getParent()).getIdentifier() + "."
                    + javaElement.getElementName();
        } else if (javaElement instanceof IField) {
            return resolver.toRecType((IType) javaElement.getParent()).getIdentifier() + "."
                    + javaElement.getElementName();
        }
        throw new IllegalArgumentException(javaElement.toString());
    }

    /**
     * @param feedback
     *            Feedback to be either newly stored or updated.
     */
    private void storeFeedback(final IUserFeedback feedback) {
        if (feedback.getDocumentId() == null) {
            server.post(feedback);
        } else {
            server.put("feedback", feedback.getDocumentId(), feedback);
        }
    }

}
