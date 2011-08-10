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
package org.eclipse.recommenders.server.extdoc.types;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.rcp.extdoc.IServerType;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;

import com.google.gson.annotations.SerializedName;

public final class SocialBookmarks implements IServerType {

    @SerializedName("_id")
    private String documentId;
    @SerializedName("_rev")
    private String rev;

    private String element;

    private final List<SocialBookmark> bookmarks = new LinkedList<SocialBookmark>();

    public static SocialBookmarks create(final String element) {
        final SocialBookmarks bookmarks = new SocialBookmarks();
        bookmarks.element = element;
        bookmarks.validate();
        return bookmarks;
    }

    public SocialBookmark addBookmark(final String text, final String description, final String url) {
        final String userId = UUIDHelper.getUUID();
        final SocialBookmark bookmark = SocialBookmark.create(userId, text, description, url);
        bookmarks.add(bookmark);
        return bookmark;
    }

    public List<SocialBookmark> getBookmarks() {
        return bookmarks;
    }

    public String getDocumentId() {
        return documentId;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(!element.isEmpty());
    }

}
