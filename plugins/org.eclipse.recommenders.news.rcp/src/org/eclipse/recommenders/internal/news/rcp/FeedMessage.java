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
import java.util.Objects;

import org.eclipse.recommenders.news.rcp.IFeedMessage;

public class FeedMessage implements IFeedMessage {

    private final String id;
    private final Date date;
    private final String description;
    private final String title;
    private final URL url;

    private boolean read;

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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        FeedMessage that = (FeedMessage) other;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public boolean isRead() {
        return read;
    }

    @Override
    public void setRead(boolean read) {
        this.read = read;
    }
}
