package org.eclipse.recommenders.rcp.extdoc.features;

import java.util.Collection;

public interface IUserFeedback {

    IRatingSummary getRatingSummary();

    Collection<? extends IComment> getComments();

    void addRating(IRating rating);

    void addComment(IComment comment);

}
