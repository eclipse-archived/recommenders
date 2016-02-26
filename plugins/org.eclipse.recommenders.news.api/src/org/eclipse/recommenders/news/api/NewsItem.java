/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.news.api;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Date;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

public final class NewsItem {

    private final String title;

    private final Date date;

    private final URI uri;

    private final String id;

    public NewsItem(String title, Date date, URI uri, @Nullable String id) {
        this.title = requireNonNull(title);
        this.date = requireNonNull(date);
        this.uri = requireNonNull(uri);
        this.id = id != null ? id : uri.toString();
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public URI getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        NewsItem that = (NewsItem) other;
        return this.title.equals(that.title) && this.date.equals(that.date) && this.uri.equals(that.uri)
                && this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, date, uri, id);
    }
}
