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

import junit.framework.Assert;

import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestProvider;
import org.junit.Test;

public final class GenericServerTest {

    final GenericServer server = new GenericServer();
    final IProvider provider = new TestProvider();

    final Object object = new Object() {
        @Override
        public int hashCode() {
            return 1;
        }
    };

    static {
        ServerUtils.initServer();
    }

    @Test
    public void testComments() {
        final IComment comment = server.addComment(object, "Test text", provider);
        final List<IComment> comments = server.getComments(object, provider);

        Assert.assertFalse(comments.isEmpty());
        Assert.assertTrue(comments.contains(comment));
    }

    @Test
    public void testRatings() {
        server.addRating(object, 4, provider);
        final int avg = server.getAverageRating(object, provider);
    }
}
