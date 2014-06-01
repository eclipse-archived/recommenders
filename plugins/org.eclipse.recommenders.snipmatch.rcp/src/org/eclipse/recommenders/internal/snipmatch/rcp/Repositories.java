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

import static org.eclipse.recommenders.internal.snipmatch.rcp.RepositoryConfigurations.findMatchingProvider;
import static org.eclipse.recommenders.internal.snipmatch.rcp.SnipmatchRcpModule.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryClosedEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryProvider;
import org.eclipse.recommenders.utils.Openable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Repositories implements IRcpService, Openable, Closeable {

    private File basedir;
    private Set<ISnippetRepository> repositories = Sets.newHashSet();
    private SnipmatchRcpPreferences prefs;
    private ImmutableSet<ISnippetRepositoryProvider> providers;

    @Inject
    public Repositories(@Named(SNIPPET_REPOSITORY_BASEDIR) File basedir, SnipmatchRcpPreferences prefs, EventBus bus,
            @Named(SNIPPET_REPOSITORY_PROVIDERS) ImmutableSet<ISnippetRepositoryProvider> providers) {
        bus.register(this);
        this.providers = providers;
        this.basedir = basedir;
        this.prefs = prefs;
    }

    @Override
    @PostConstruct
    public void open() throws IOException {
        repositories.clear();
        for (ISnippetRepositoryConfiguration config : prefs.getConfigurations()) {
            if (!config.isEnabled()) {
                continue;
            }
            ISnippetRepositoryProvider provider = findMatchingProvider(config, providers).orNull();
            if (provider != null) {
                ISnippetRepository repo = provider.create(config, basedir).orNull();
                if (repo != null) {
                    repo.open();
                    repositories.add(repo);
                }
            }
        }
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        for (ISnippetRepository repo : repositories) {
            repo.close();
        }
        repositories.clear();
    }

    public Set<ISnippetRepository> getRepositories() {
        return repositories;
    }

    @Subscribe
    public void onEvent(SnippetRepositoryConfigurationChangedEvent e) throws IOException {
        close();
        // TODO: Since opening a snippet repository is potentially expensive only the affected ones should be processed.
        open();
    }

    /**
     * Triggered when a snippet repository URL was changed (most likely in the a preference page).
     * <p>
     * Clients of this event should be an instance of {@link ISnippetRepository}. Other clients should have a look at
     * {@link SnippetRepositoryClosedEvent} and {@link SnippetRepositoryClosedEvent}. Clients of this event may consider
     * refreshing themselves whenever they receive this event. Clients get notified in a background process.
     */
    public static class SnippetRepositoryConfigurationChangedEvent {
        // TODO: Event should contain which configuration was changed.
    }

}
