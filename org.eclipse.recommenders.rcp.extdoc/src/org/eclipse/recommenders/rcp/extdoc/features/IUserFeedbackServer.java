package org.eclipse.recommenders.rcp.extdoc.features;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.rcp.extdoc.IProvider;

public interface IUserFeedbackServer {

    IUserFeedback getUserFeedback(IJavaElement javaElement, IProvider provider);

    IRating addRating(int stars, IJavaElement javaElement, IProvider provider);

    IComment addComment(String text, IJavaElement javaElement, IProvider provider);

}
