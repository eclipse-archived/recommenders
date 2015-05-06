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

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.internal.snipmatch.rcp.LogMessages.ERROR_SERVICE_NOT_RUNNING;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.SnippetRepositoryClosedEvent;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Openable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;

public class Repositories extends AbstractIdleService implements IRcpService, Openable, Closeable {

    private final SnippetRepositoryConfigurations configurations;
    private final SnipmatchRcpPreferences prefs;

    private Set<ISnippetRepository> repositories = Sets.newHashSet();

    @Inject
    public Repositories(EventBus bus, SnippetRepositoryConfigurations configurations, SnipmatchRcpPreferences prefs) {
        bus.register(this);
        this.prefs = prefs;
        this.configurations = configurations;
    }

    @Override
    @PostConstruct
    public void open() throws IOException {
        startAsync();
    }

    @Override
    protected void startUp() throws Exception {
        repositories.clear();
        for (SnippetRepositoryConfiguration config : configurations.getRepos()) {
            if (!prefs.isRepositoryEnabled(config)) {
                continue;
            }
            ISnippetRepository repo = config.createRepositoryInstance();
            repo.open();
            repositories.add(repo);
        }
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        stopAsync();
    }

    @Override
    protected void shutDown() throws Exception {
        for (ISnippetRepository repo : repositories) {
            repo.close();
        }
        repositories.clear();
    }

    public void reload() throws Exception {
        if (!isRunning()) {
            log(ERROR_SERVICE_NOT_RUNNING);
        }
        shutDown();
        startUp();
    }

    public Set<ISnippetRepository> getRepositories() {
        if (!isRunning()) {
            log(ERROR_SERVICE_NOT_RUNNING);
            return ImmutableSet.of();
        }
        return repositories;
    }

    public Optional<ISnippetRepository> getRepository(String id) {
        if (!isRunning()) {
            log(ERROR_SERVICE_NOT_RUNNING);
            return absent();
        }

        for (ISnippetRepository repo : repositories) {
            if (repo.getId().equals(id)) {
                return of(repo);
            }
        }
        return absent();
    }

    @Subscribe
    public void onEvent(SnippetRepositoryConfigurationChangedEvent e) throws Exception {
        reload();
        // TODO: Since opening a snippet repository is potentially expensive only the affected ones should be processed.
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
