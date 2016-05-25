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
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.LogMessages;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Logs;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class SnipmatchRcpPreferences {

    private static final String SEPARATOR = ";"; //$NON-NLS-1$
    private final EventBus bus;
    private Set<String> disabledRepositories = Sets.newHashSet();

    @Inject
    public SnipmatchRcpPreferences(EventBus bus) {
        this.bus = bus;
    }

    @Inject
    public void setDisabledRepositoryConfigurations(
            @Preference(Constants.PREF_DISABLED_REPOSITORY_CONFIGURATIONS) String newDisabledRepositoryConfiguration)
            throws Exception {
        if (newDisabledRepositoryConfiguration == null) {
            return;
        }

        Set<String> newDisabledRepositories = splitDisabledRepositoryString(newDisabledRepositoryConfiguration);
        if (disabledRepositories.equals(newDisabledRepositories)) {
            return;
        }

        disabledRepositories = newDisabledRepositories;
        bus.post(new Repositories.SnippetRepositoryConfigurationChangedEvent());
    }

    public boolean isRepositoryEnabled(SnippetRepositoryConfiguration config) {
        return !disabledRepositories.contains(config.getId());
    }

    public void setRepositoryEnabled(SnippetRepositoryConfiguration config, boolean enabled) {
        Set<String> temp = Sets.newHashSet(disabledRepositories);
        if (enabled) {
            temp.remove(config.getId());
        } else {
            temp.add(config.getId());
        }
        store(joinDisabledRepositoriesToString(temp));
    }

    public static Set<String> splitDisabledRepositoryString(String disabledRepositoryConfigurations) {
        Iterable<String> split = Splitter.on(SEPARATOR).omitEmptyStrings().split(disabledRepositoryConfigurations);
        return Sets.newHashSet(split);
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
