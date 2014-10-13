/**
 * Copyright (c) 2014 Olav Lenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.snipmatch.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnipmatchRcpModelFactory;
import org.eclipse.recommenders.utils.Logs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DefaultGitSnippetRepositoryConfigurations {

    public static List<EclipseGitSnippetRepositoryConfiguration> fetchDefaultConfigurations() {
        List<EclipseGitSnippetRepositoryConfiguration> defaultConfigurations = Lists.newArrayList();
        Set<String> addedUrls = Sets.newHashSet();

        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS);
        for (IConfigurationElement element : elements) {
            try {
                String name = element.getAttribute(EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_NAME);
                String description = element
                        .getAttribute(EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_DESCRIPTION);
                boolean enabled = Boolean.valueOf(element
                        .getAttribute(EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_ENABLED));
                String url = element.getAttribute(EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_URL);
                if (addedUrls.contains(url)) {
                    Logs.log(LogMessages.WARNING_DEFAULT_GIT_REPO_URL_DUPLICATE, name, url);
                    continue;
                }
                String pushUrl = element.getAttribute(EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_PUSH_URL);
                String pushBranchPrefix = element
                        .getAttribute(EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_PUSH_BRANCH_PREFIX);

                defaultConfigurations.add(createConfiguration(name, description, enabled, url, pushUrl,
                        pushBranchPrefix));
                addedUrls.add(url);
            } catch (Exception e) {
                Logs.log(LogMessages.ERROR_LOADING_DEFAULT_GIT_REPO_CONFIGURATION, e);
            }
        }

        return defaultConfigurations;
    }

    private static EclipseGitSnippetRepositoryConfiguration createConfiguration(String name, String description,
            boolean enabled, String url, String pushUrl, String pushBranchPrefix) {
        EclipseGitSnippetRepositoryConfiguration configuration = SnipmatchRcpModelFactory.eINSTANCE
                .createEclipseGitSnippetRepositoryConfiguration();

        configuration.setName(name);
        configuration.setDescription(description);
        configuration.setEnabled(enabled);
        configuration.setUrl(url);
        configuration.setPushUrl(pushUrl);
        configuration.setPushBranchPrefix(pushBranchPrefix);
        return configuration;
    }

}
