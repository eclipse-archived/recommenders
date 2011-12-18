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
package org.eclipse.recommenders.extdoc.transport.types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.extdoc.rcp.IProvider;
import org.eclipse.recommenders.extdoc.rcp.IServerType;
import org.eclipse.recommenders.internal.extdoc.transport.AbstractFeedbackServer;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.recommenders.utils.rcp.UUIDHelper;

import com.google.gson.annotations.SerializedName;

public final class SocialBookmarks implements IServerType {

    @SerializedName("_id")
    private String documentId;
    @SerializedName("_rev")
    private String rev;

    private String element;

    private final List<SocialBookmark> bookmarks = new LinkedList<SocialBookmark>();

    private transient boolean isDirty = true;

    public static SocialBookmarks create(final String element) {
        final SocialBookmarks bookmarks = new SocialBookmarks();
        bookmarks.element = element;
        bookmarks.validate();
        return bookmarks;
    }

    public SocialBookmark addBookmark(final String text, final String url) {
        final String userId = UUIDHelper.getUUID();
        final SocialBookmark bookmark = SocialBookmark.create(userId, text, url);
        bookmarks.add(bookmark);
        isDirty = true;
        return bookmark;
    }

    // TODO: name is the same as the element field, although wrapped in an
    // interface.
    public List<SocialBookmark> getBookmarks(final IName name, final AbstractFeedbackServer server,
            final IProvider provider) {
        if (isDirty) {
            for (final SocialBookmark bookmark : bookmarks) {
                bookmark.setUserFeedback(server.getUserFeedback(name, bookmark.getUrl(), provider));
            }
            Collections.sort(bookmarks);
            isDirty = false;
        }
        return bookmarks;
    }

    public boolean isEmpty() {
        return bookmarks.isEmpty();
    }

    public String getDocumentId() {
        return documentId;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(!element.isEmpty());
    }

}
