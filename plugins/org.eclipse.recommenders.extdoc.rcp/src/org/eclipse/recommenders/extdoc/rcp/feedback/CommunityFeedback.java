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
package org.eclipse.recommenders.extdoc.rcp.feedback;

import org.eclipse.recommenders.extdoc.rcp.IProvider;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Preconditions;

/**
 * Pre-loads community feedback for a particular location and acts as a factory
 * for SWT displays of the feedback.
 */
public final class CommunityFeedback {

    private IProvider provider;
    private IUserFeedbackServer server;
    private IName element;
    private String keyAppendix;
    private IUserFeedback feedback;

    /**
     * @param element
     *            The element of the selection.
     * @param keyAppendix
     *            In case there are multiple feedback parts for the associated
     *            provider the particular part can be identified through this.
     * @param provider
     *            The provider hosting the community features.
     * @param server
     *            The source and target for user feedback.
     * @return A factory for displaying a comments section and/or stars ratings.
     */
    public static CommunityFeedback create(final IName element, final String keyAppendix, final IProvider provider,
            final IUserFeedbackServer server) {
        return element == null ? null : create(element, keyAppendix, provider, server,
                server.getUserFeedback(element, keyAppendix, provider));
    }

    /**
     * @param element
     *            The element of the selection.
     * @param keyAppendix
     *            In case there are multiple feedback parts for the associated
     *            provider the particular part can be identified through this.
     * @param provider
     *            The provider hosting the community features.
     * @param server
     *            The source and target for user feedback.
     * @param feedback
     *            An object storing feedback data received from the server.
     * @return A factory for displaying a comments section and/or stars ratings.
     */
    public static CommunityFeedback create(final IName element, final String keyAppendix, final IProvider provider,
            final IUserFeedbackServer server, final IUserFeedback feedback) {
        if (element == null) {
            return null;
        }
        final CommunityFeedback features = new CommunityFeedback();
        features.provider = provider;
        features.server = Preconditions.checkNotNull(server);
        features.element = element;
        features.keyAppendix = keyAppendix;
        features.feedback = feedback;
        return features;
    }

    /**
     * @param parent
     *            The composite to which the comments shall be appended.
     * @return A composite displaying comments and allowing to add new ones.
     */
    public CommentsComposite loadCommentsComposite(final Composite parent) {
        return CommentsComposite.create(element, keyAppendix, provider, feedback, server, parent);
    }

    /**
     * @param parent
     *            The composite to which the stars rating widget shall be
     *            appended.
     * @return A composite displaying a 5-star rating widget.
     */
    public StarsRatingComposite loadStarsRatingComposite(final Composite parent) {
        return new StarsRatingComposite(element, keyAppendix, provider, feedback, server, parent);
    }

}
