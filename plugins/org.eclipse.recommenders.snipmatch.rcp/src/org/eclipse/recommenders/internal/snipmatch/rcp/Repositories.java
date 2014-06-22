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

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryClosedEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Openable;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class Repositories implements IRcpService, Openable, Closeable {

    private Set<ISnippetRepository> repositories = Sets.newHashSet();
    private SnippetRepositoryConfigurations configurations;

    @Inject
    public Repositories(EventBus bus, SnippetRepositoryConfigurations configurations) {
        bus.register(this);
        this.configurations = configurations;
    }

    @Override
    @PostConstruct
    public void open() throws IOException {
        repositories.clear();
        for (SnippetRepositoryConfiguration config : configurations.getRepos()) {
            if (!config.isEnabled()) {
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
