/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import java.util.Map;

import org.eclipse.recommenders.snipmatch.ISnippetRepositoryConfiguration;

import com.google.common.collect.Maps;

public class EclipseGitSnippetRepositoryConfiguration implements ISnippetRepositoryConfiguration {

    private String name;
    private String location;
    private boolean enabled;

    public EclipseGitSnippetRepositoryConfiguration(String name, String repositoryUrl, boolean enabled) {
        this.name = name;
        this.location = repositoryUrl;
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getRepositoryUrl() {
        return location;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getDescription() {
        return Messages.ECLIPSE_GIT_SNIPPET_REPOSITORY_CONFIGURATION_DESCRIPTION;
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = Maps.newHashMap();
        attributes.put(Messages.ECLIPSE_GIT_SNIPPET_REPOSITORY_CONFIGURATION_ATTRIBUTE_NAME_URL, getRepositoryUrl());
        return attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EclipseGitSnippetRepositoryConfiguration other = (EclipseGitSnippetRepositoryConfiguration) obj;
        if (enabled != other.enabled)
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
