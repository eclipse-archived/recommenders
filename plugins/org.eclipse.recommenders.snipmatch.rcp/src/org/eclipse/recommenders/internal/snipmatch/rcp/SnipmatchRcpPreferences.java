/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - introduce ISnippetRepositoryConfiguration.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.RepositoryConfigurations.fromPreferenceString;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories.SnippetRepositoryConfigurationChangedEvent;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryProvider;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.name.Named;

@SuppressWarnings("restriction")
public class SnipmatchRcpPreferences {

    private ImmutableList<ISnippetRepositoryConfiguration> configurations;
    private ImmutableSet<ISnippetRepositoryProvider> providers;
    private EventBus bus;

    public SnipmatchRcpPreferences(EventBus bus,
            @Named(SnipmatchRcpModule.SNIPPET_REPOSITORY_PROVIDERS) ImmutableSet<ISnippetRepositoryProvider> providers) {
        this.bus = bus;
        this.providers = providers;
    }

    @Inject
    public void setConfigurations(@Preference(Constants.PREF_SNIPPETS_REPO) String newValue) {
        List<ISnippetRepositoryConfiguration> old = configurations;
        configurations = fromPreferenceString(newValue, providers);
        if (old != null && !old.equals(configurations)) {
            bus.post(new SnippetRepositoryConfigurationChangedEvent());
        }
    }

    public ImmutableList<ISnippetRepositoryConfiguration> getConfigurations() {
        return configurations;
    }

    public static void updateDefaultPreferences(Collection<ISnippetRepositoryProvider> providers) {
        List<ISnippetRepositoryConfiguration> configurations = collectConfigurationsFromProviders(providers);
        String preferenceString = RepositoryConfigurations.toPreferenceString(configurations, providers);

        ScopedPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, Constants.BUNDLE_ID);
        store.setDefault(Constants.PREF_SNIPPETS_REPO, preferenceString);
    }

    public static void updatePreferences(Collection<ISnippetRepositoryProvider> providers) {
        List<ISnippetRepositoryConfiguration> newConfigs = collectConfigurationsFromProviders(providers);

        ScopedPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, Constants.BUNDLE_ID);
        String oldPreferenceString = store.getString(Constants.PREF_SNIPPETS_REPO);
        List<ISnippetRepositoryConfiguration> storedConfigs = RepositoryConfigurations.fromPreferenceString(
                oldPreferenceString, providers);

        for (ISnippetRepositoryConfiguration newConfig : newConfigs) {
            for (ISnippetRepositoryConfiguration storedConfig : storedConfigs) {
                if (!storedConfig.getName().equals(newConfig.getName())) {
                    storedConfigs.add(newConfig);
                    continue;
                }
            }
        }

        String newPreferenceString = RepositoryConfigurations.toPreferenceString(storedConfigs, providers);

        if (!oldPreferenceString.equals(newPreferenceString)) {
            store.setValue(Constants.PREF_SNIPPETS_REPO, newPreferenceString);
        }
    }

    private static List<ISnippetRepositoryConfiguration> collectConfigurationsFromProviders(
            Collection<ISnippetRepositoryProvider> providers) {
        List<ISnippetRepositoryConfiguration> newConfigs = Lists.newArrayList();
        for (ISnippetRepositoryProvider provider : providers) {
            newConfigs.addAll(provider.getDefaultConfigurations());
        }
        return newConfigs;
    }
}
