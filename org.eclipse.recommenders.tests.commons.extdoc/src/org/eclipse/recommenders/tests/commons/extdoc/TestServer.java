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
package org.eclipse.recommenders.tests.commons.extdoc;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;

public final class TestServer implements IStarsRatingsServer, ICommentsServer {

    @Override
    public int getAverageRating(final Object object, final IProvider provider) {
        return 0;
    }

    @Override
    public int getUserRating(final Object object, final IProvider provider) {
        return 0;
    }

    @Override
    public void addRating(final Object object, final int stars, final IProvider provider) {
    }

    @Override
    public List<IComment> getComments(final Object object, final IProvider provider) {
        return Collections.emptyList();
    }

    @Override
    public IComment addComment(final Object object, final String text, final IProvider provider) {
        return new IComment() {
            @Override
            public String getText() {
                return text;
            }

            @Override
            public Date getDate() {
                return new Date();
            }
        };
    }
}