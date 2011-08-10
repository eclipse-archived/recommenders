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

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.rcp.extdoc.IServerType;

public final class SocialBookmark implements IServerType, Comparable<SocialBookmark> {

    private String userId;
    private String title;
    private String description;
    private String url;

    public static SocialBookmark create(final String userId, final String title, final String description,
            final String url) {
        final SocialBookmark bookmark = new SocialBookmark();
        bookmark.userId = userId;
        bookmark.title = title;
        bookmark.description = description;
        bookmark.url = url;
        bookmark.validate();
        return bookmark;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int compareTo(final SocialBookmark arg0) {
        return 0;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(!userId.isEmpty() && !title.isEmpty() && !description.isEmpty() && !url.isEmpty());
    }

}
