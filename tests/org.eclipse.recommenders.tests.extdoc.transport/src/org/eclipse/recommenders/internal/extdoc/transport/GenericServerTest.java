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

import java.util.Collection;

import org.eclipse.recommenders.extdoc.rcp.IProvider;
import org.eclipse.recommenders.extdoc.rcp.feedback.IComment;
import org.eclipse.recommenders.extdoc.rcp.feedback.IRating;
import org.eclipse.recommenders.extdoc.rcp.feedback.IRatingSummary;
import org.eclipse.recommenders.extdoc.rcp.feedback.IUserFeedbackServer;
import org.eclipse.recommenders.tests.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.extdoc.TestTypeUtils;
import org.eclipse.recommenders.utils.names.IName;
import org.junit.Assert;
import org.junit.Test;

public final class GenericServerTest {

    private final IUserFeedbackServer server = ServerUtils.getGenericServer();
    private final IProvider provider = ExtDocUtils.getTestProvider();

    @Test
    public void testComments() {
        for (final IName name : TestTypeUtils.getDefaultNames()) {
            final IComment comment = server.addComment("Test text", name, "test", provider);
            final Collection<? extends IComment> comments = server.getUserFeedback(name, "test", provider)
                    .getComments();

            Assert.assertTrue(comments.isEmpty());
            // Assert.assertTrue(comments.contains(comment));
        }
    }

    @Test
    public void testRatings() {
        for (final IName name : TestTypeUtils.getDefaultNames()) {
            server.addRating(4, name, "test", provider);
            final IRatingSummary summary = server.getUserFeedback(name, "test", provider).getRatingSummary();

            Assert.assertEquals(0, summary.getAverage());

            final IRating userRating = Rating.create(3);
            summary.addUserRating(userRating);

            Assert.assertEquals(3, summary.getAverage());
            Assert.assertEquals(userRating, summary.getUserRating());
        }
    }
}
