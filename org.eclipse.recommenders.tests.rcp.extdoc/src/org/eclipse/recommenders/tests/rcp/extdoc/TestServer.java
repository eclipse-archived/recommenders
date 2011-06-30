package org.eclipse.recommenders.tests.rcp.extdoc;

import java.util.List;

import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;

public final class TestServer implements IStarsRatingsServer, ICommentsServer {

    @Override
    public int getAverageRating(final Object object) {
        return 0;
    }

    @Override
    public int getUserRating(final Object object) {
        return 0;
    }

    @Override
    public void addRating(final Object object, final int stars) {
    }

    @Override
    public List<IComment> getComments(final Object object) {
        return null;
    }

    @Override
    public IComment addComment(final Object object, final String text) {
        return null;
    }
}