/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.eclipse.recommenders.internal.news.rcp.Constants.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.recommenders.internal.news.rcp.l10n.LogMessages;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;

import com.google.common.base.Preconditions;

public class FeedDescriptor implements Comparable<FeedDescriptor> {

    private final boolean defaultRepository;
    private final String id;

    @Nullable
    private final URI uri;

    private final String name;
    private final long pollingInterval;

    @Nullable
    private final String contributedBy;

    private boolean enabled;

    public FeedDescriptor(FeedDescriptor that) {
        this(that.getId(), that.getUri().toString(), that.getName(), that.isEnabled(), that.isDefaultRepository(),
                that.getPollingInterval(), that.getContributedBy());
    }

    public FeedDescriptor(String uri, String name, long pollingInterval) {
        this(uri, uri, name, true, false, pollingInterval, null);
    }

    private FeedDescriptor(String id, String uri, String name, boolean enabled, boolean defaultRepository,
            long pollingInterval, @Nullable String contributedBy) {
        Objects.requireNonNull(id);
        Preconditions.checkArgument(isUrlValid(uri), Messages.FEED_DESCRIPTOR_MALFORMED_URL);

        this.id = id;
        this.uri = stringToUrl(uri);
        this.name = name;
        this.enabled = enabled;
        this.defaultRepository = defaultRepository;
        this.pollingInterval = pollingInterval;
        this.contributedBy = contributedBy;
    }

    @Nullable
    public String getContributedBy() {
        return contributedBy;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public URI getUri() {
        return uri;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public boolean isDefaultRepository() {
        return defaultRepository;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        FeedDescriptor that = (FeedDescriptor) other;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public static boolean isUrlValid(String url) {
        URL u;
        try {
            u = new URL(url);
            u.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }

    @Nullable
    private static URI stringToUrl(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            Logs.log(LogMessages.ERROR_FEED_MALFORMED_URL, url);
            return null;
        }
    }

    @Override
    public int compareTo(FeedDescriptor that) {
        return this.getName().compareTo(that.getName());
    }

    public static FeedDescriptor fromConfigurationElement(IConfigurationElement config, String contributedBy) {
        String id = config.getAttribute(ATTRIBUTE_ID);
        String uri = config.getAttribute(ATTRIBUTE_URI);
        String name = config.getAttribute(ATTRIBUTE_NAME);
        String pollingIntervalAttribute = config.getAttribute(ATTRIBUTE_POLLING_INTERVAL);
        long pollingInterval;
        if (pollingIntervalAttribute != null) {
            pollingInterval = Long.parseLong(pollingIntervalAttribute);
        } else {
            pollingInterval = HOURS.toMinutes(8);
        }
        String enabledByDefaultAttribute = config.getAttribute(Constants.ATTRIBUTE_ENABLED_BY_DEFAULT);
        boolean enabledByDefault;
        if (enabledByDefaultAttribute != null) {
            enabledByDefault = Boolean.parseBoolean(enabledByDefaultAttribute);
        } else {
            enabledByDefault = true;
        }

        return new FeedDescriptor(id, uri, name, enabledByDefault, true, pollingInterval, contributedBy);
    }
}
