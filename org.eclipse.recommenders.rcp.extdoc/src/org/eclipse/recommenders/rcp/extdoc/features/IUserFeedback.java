package org.eclipse.recommenders.rcp.extdoc.features;

import java.util.Collection;

import org.eclipse.recommenders.commons.utils.names.IName;

public interface IUserFeedback {

    IRatingSummary getRatingSummary();

    Collection<? extends IComment> getComments();

    void addRating(IRating rating);

    void addComment(IComment comment);

    IName getElement();

    String getRevision();

}
