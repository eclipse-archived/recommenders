package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.CommentsDialog;
import org.eclipse.recommenders.rcp.extdoc.features.CommentsIcon;
import org.eclipse.recommenders.rcp.extdoc.features.DeleteIcon;
import org.eclipse.recommenders.rcp.extdoc.features.EditIcon;
import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;
import org.eclipse.recommenders.rcp.extdoc.features.StarsRating;

public final class CommunityUtil {

    private CommunityUtil() {
    }

    public static String getAllFeatures(final IJavaElement element, final AbstractBrowserProvider provider,
            final Dialog editDialog, final IStarsRatingsServer server) {
        final StringBuilder builder = new StringBuilder(128);
        builder.append(provider.addListenerAndGetHtml(getCommentsIcon(element, element.getElementName(), provider)));
        builder.append(provider.addListenerAndGetHtml(getEditIcon(editDialog)));
        if (provider instanceof IDeletionProvider) {
            builder.append(provider.addListenerAndGetHtml(getDeleteIcon(element, element.getElementName(),
                    (IDeletionProvider) provider)));
        }
        builder.append(provider.addListenerAndGetHtml(getStarsRating(element, provider, server)));
        return builder.toString();
    }

    public static String getAllFeaturesButDelete(final IJavaElement element, final AbstractBrowserProvider provider,
            final Dialog editDialog, final IStarsRatingsServer server) {
        final StringBuilder builder = new StringBuilder(128);
        builder.append(provider.addListenerAndGetHtml(getCommentsIcon(element, element.getElementName(), provider)));
        builder.append(provider.addListenerAndGetHtml(getEditIcon(editDialog)));
        builder.append(provider.addListenerAndGetHtml(getStarsRating(element, provider, server)));
        return builder.toString();
    }

    public static EditIcon getEditIcon(final Dialog editDialog) {
        return new EditIcon(editDialog);
    }

    public static DeleteIcon getDeleteIcon(final Object object, final String objectName,
            final IDeletionProvider provider) {
        return new DeleteIcon(provider, object, objectName);
    }

    public static CommentsIcon getCommentsIcon(final Object object, final String objectName, final IProvider provider) {
        final CommentsDialog commentsDialog = new CommentsDialog(provider.getShell(), null, provider, object,
                objectName);
        return new CommentsIcon(commentsDialog);
    }

    public static StarsRating getStarsRating(final Object object, final IProvider provider,
            final IStarsRatingsServer server) {
        return new StarsRating(object, server, provider);
    }
}
