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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Logs;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SnipmatchRcpPreferences {

    static final String SEPARATOR = ";"; //$NON-NLS-1
    private Set<String> disabledRepositories = Sets.newHashSet();

    @Inject
    public void setDisabledRepositoryConfigurations(
            @Preference(Constants.PREF_DISABLED_REPOSITORY_CONFIGURATIONS) String newDisabledRepositoryConfiguration)
            throws Exception {
        if (newDisabledRepositoryConfiguration == null) {
            return;
        }
        List<String> disabledRepositoryConfigurations = splitDisabledRepositoryString(newDisabledRepositoryConfiguration);
        disabledRepositories.clear();
        disabledRepositories.addAll(disabledRepositoryConfigurations);
    }

    public boolean isRepositoryEnabled(SnippetRepositoryConfiguration config) {
        return !disabledRepositories.contains(config.getId());
    }

    public void setRepositoryEnabled(SnippetRepositoryConfiguration config, boolean enabled) {
        if (enabled) {
            disabledRepositories.remove(config.getId());
        } else {
            disabledRepositories.add(config.getId());
        }
        store(joinDisabledRepositoriesToString(disabledRepositories));
    }

    public static List<String> splitDisabledRepositoryString(String disabledRepositoryConfigurations) {
        Iterable<String> split = Splitter.on(SEPARATOR).omitEmptyStrings().split(disabledRepositoryConfigurations);
        return Lists.newArrayList(split);
    }

    public static String joinDisabledRepositoriesToString(Collection<String> disabledRepositoryConfigurations) {
        return Joiner.on(SnipmatchRcpPreferences.SEPARATOR).join(disabledRepositoryConfigurations);
    }

    public static void store(String disabledRepositoryConfigurations) {
        IEclipsePreferences s = InstanceScope.INSTANCE.getNode(Constants.BUNDLE_ID);
        s.put(Constants.PREF_DISABLED_REPOSITORY_CONFIGURATIONS, disabledRepositoryConfigurations);
        try {
            s.flush();
        } catch (BackingStoreException e) {
            Logs.log(LogMessages.ERROR_STORING_DISABLED_REPOSITORY_CONFIGURATIONS, e);
        }
    }
}
