package org.eclipse.recommenders.internal.server.extdoc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.server.extdoc.types.Comment;

import com.sun.jersey.api.client.GenericType;

public abstract class AbstractCommentsServer extends AbstractRatingsServer implements ICommentsServer {

    @Override
    public final List<IComment> getComments(final Object object, final IProvider provider) {
        final String path = Server.buildPath("comments", provider.getClass().getSimpleName(), "object",
                String.valueOf(object.hashCode()), false);
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
