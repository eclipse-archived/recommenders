/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.net.URL;
import java.util.Date;

import org.eclipse.recommenders.news.rcp.IFeedMessage;

import com.google.common.base.Objects;

public class FeedMessage implements IFeedMessage {

    private final String id;
    private final Date date;
    private final String description;
    private final String title;
    private final URL url;

    public FeedMessage(String id, Date date, String description, String title, URL url) {
        super();
        this.id = id;
        this.date = date;
        this.description = description;
        this.title = title;
        this.url = url;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FeedMessage that = (FeedMessage) obj;
        return Objects.equal(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
