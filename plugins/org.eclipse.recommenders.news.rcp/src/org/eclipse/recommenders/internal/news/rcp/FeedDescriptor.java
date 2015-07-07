/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.base.Preconditions;

public class FeedDescriptor {

    private final IConfigurationElement config;
    private boolean enabled;

    public FeedDescriptor(FeedDescriptor that) {
        this(that.config, that.enabled);
    }

    public FeedDescriptor(IConfigurationElement config, boolean enabled) {
        this.config = config;
        this.enabled = enabled;
        Preconditions.checkNotNull(getId());
        Preconditions.checkArgument(isUrlValid(config.getAttribute("url")), Messages.FEED_DESCRIPTOR_MALFORMED_URL); //$NON-NLS-1$
    }

    public String getId() {
        return config.getAttribute("id"); //$NON-NLS-1$
    }

    public String getName() {
        return config.getAttribute("name"); //$NON-NLS-1$
    }

    public URL getUrl() {
        return stringToUrl(config.getAttribute("url")); //$NON-NLS-1$
    }

    public String getDescription() {
        return config.getAttribute("description"); //$NON-NLS-1$
    }

    public String getPollingInterval() {
        return config.getAttribute("pollingInterval"); //$NON-NLS-1$
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Image getIcon() {
        String iconPath = config.getAttribute("icon"); //$NON-NLS-1$
        if (iconPath != null) {
            return AbstractUIPlugin.imageDescriptorFromPlugin(Constants.PLUGIN_ID, iconPath).createImage();
        }
        return null;
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

    private boolean isUrlValid(String url) {
        URL u;
        try {
            u = new URL(url);
            u.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }

    private URL stringToUrl(String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            // should never happen
            return null;
        }
    }
}
