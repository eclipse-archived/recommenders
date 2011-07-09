package org.eclipse.recommenders.internal.server.extdoc;

import java.util.Collections;
import java.util.List;

import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.server.extdoc.types.Comment;

public abstract class AbstractCommentsServer extends AbstractRatingsServer implements ICommentsServer {

    @Override
    public final List<IComment> getComments(final Object object, final IProvider provider) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public final IComment addComment(final Object object, final String text, final IProvider provider) {
        final IComment comment = Comment.create(object, text);
        return comment;
    }

}
