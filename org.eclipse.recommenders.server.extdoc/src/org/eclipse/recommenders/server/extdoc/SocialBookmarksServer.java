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
package org.eclipse.recommenders.server.extdoc;

import java.util.List;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.internal.server.extdoc.AbstractFeedbackServer;
import org.eclipse.recommenders.server.extdoc.types.SocialBookmark;
import org.eclipse.recommenders.server.extdoc.types.SocialBookmarks;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public final class SocialBookmarksServer extends AbstractFeedbackServer {

    @Inject
    public SocialBookmarksServer(final ICouchDbServer server, final UsernameProvider usernameProvider) {
        super(server, usernameProvider);
    }

    public SocialBookmarks getBookmarks(final IName element) {
        final String elementId = element.getIdentifier();
        final List<SocialBookmarks> bookmarks = getServer().getRows("bookmarks", ImmutableMap.of("element", elementId),
                new GenericType<GenericResultObjectView<SocialBookmarks>>() {
                });
        return bookmarks == null || bookmarks.isEmpty() ? SocialBookmarks.create(elementId) : bookmarks.get(0);
    }

    public SocialBookmark addBookmark(final IName element, final String title, final String url) {
        final SocialBookmarks bookmarks = getBookmarks(element);
        final SocialBookmark bookmark = bookmarks.addBookmark(title, url);
        if (bookmarks.getDocumentId() == null) {
            getServer().post(bookmarks);
        } else {
            getServer().put("bookmarks", bookmarks.getDocumentId(), bookmarks);
        }
        return bookmark;
    }

}
