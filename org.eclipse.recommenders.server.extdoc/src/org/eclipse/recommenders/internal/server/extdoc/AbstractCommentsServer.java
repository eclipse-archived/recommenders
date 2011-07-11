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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.server.extdoc.types.Comment;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.GenericType;

public abstract class AbstractCommentsServer extends AbstractRatingsServer implements ICommentsServer {

    @Override
    public final List<IComment> getComments(final Object object, final IProvider provider) {
        final ImmutableMap<String, String> key = ImmutableMap.of("providerId", provider.getClass().getSimpleName(),
                "object", String.valueOf(object.hashCode()));
        final String path = Server.buildPath("comments", key);
        final List<Comment> rows = Server.getRows(path, new GenericType<GenericResultObjectView<Comment>>() {
        });
        return new ArrayList<IComment>(rows);
    }

    @Override
    public final IComment addComment(final Object object, final String text, final IProvider provider) {
        final IComment comment = Comment.create(provider, object, text);
        Server.post(comment);
        return comment;
    }

}
