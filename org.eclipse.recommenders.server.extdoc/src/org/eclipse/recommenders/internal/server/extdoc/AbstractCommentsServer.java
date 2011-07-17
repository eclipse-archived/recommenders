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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.UsernamePreferenceListener;
import org.eclipse.recommenders.server.extdoc.types.Comment;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.GenericType;

public abstract class AbstractCommentsServer extends AbstractRatingsServer implements ICommentsServer {

    private final UsernamePreferenceListener listener;

    public AbstractCommentsServer(final ICouchDbServer server, final UsernamePreferenceListener usernameListener) {
        super(server);
        listener = usernameListener;
    }

    @Override
    public final List<IComment> getComments(final Object object, final IProvider provider) {
        final ImmutableMap<String, String> key = ImmutableMap.of("providerId", provider.getClass().getSimpleName(),
                "object", String.valueOf(object.hashCode()));
        final List<Comment> rows = getServer().getRows("comments", key,
                new GenericType<GenericResultObjectView<Comment>>() {
                });
        if (rows == null) {
            return Collections.emptyList();
        }
        return sortComments(rows);
    }

    private List<IComment> sortComments(final List<Comment> rows) {
        final List<IComment> list = new ArrayList<IComment>(rows);
        Collections.sort(list, new Comparator<IComment>() {
            @Override
            public int compare(final IComment o1, final IComment o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        return list;
    }

    @Override
    public final IComment addComment(final Object object, final String text, final IProvider provider) {
        final IComment comment = Comment.create(provider, object, text, listener.getUsername());
        getServer().post(comment);
        return comment;
    }

}
