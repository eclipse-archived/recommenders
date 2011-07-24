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

import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IRatingSummary;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.recommenders.server.extdoc.types.Rating;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestUtils;

import org.junit.Assert;
import org.junit.Test;

public final class GenericServerTest {

    private final GenericServer server = ServerUtils.getGenericServer();
    private final IProvider provider = ExtDocUtils.getTestProvider();

    @Test
    public void testComments() {
        for (final IName name : TestUtils.getDefaultNames()) {
            final IComment comment = server.addComment("Test text", name, provider);
            final Collection<? extends IComment> comments = server.getUserFeedback(name, provider).getComments();

            Assert.assertTrue(comments.isEmpty());
            // Assert.assertTrue(comments.contains(comment));
        }
    }

    @Test
    public void testRatings() {
        for (final IName name : TestUtils.getDefaultNames()) {
            server.addRating(4, name, provider);
            final IRatingSummary summary = server.getUserFeedback(name, provider).getRatingSummary();

            Assert.assertEquals(0, summary.getAverage());

            final IRating userRating = Rating.create(3);
            summary.addUserRating(userRating);

            Assert.assertEquals(3, summary.getAverage());
            Assert.assertEquals(userRating, summary.getUserRating());
        }
    }
}
