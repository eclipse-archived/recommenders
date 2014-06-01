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
import static org.eclipse.recommenders.utils.Checks.*;

import java.io.File;
import java.util.List;

import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryProvider;
import org.eclipse.recommenders.utils.gson.GsonUtil;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.inject.Inject;

public class EclipseGitSnippetRepositoryProvider implements ISnippetRepositoryProvider {

    private static final String REPO_URL = "https://git.eclipse.org/r/recommenders/org.eclipse.recommenders.snipmatch.snippets"; //$NON-NLS-1$

    private final Gson gson = GsonUtil.getInstance();
    private EventBus bus;

    @Inject
    public EclipseGitSnippetRepositoryProvider(EventBus bus) {
        this.bus = bus;
    }

    public EclipseGitSnippetRepositoryProvider() {
    }

    @Override
    public boolean isApplicable(String identifier) {
        return EclipseGitSnippetRepositoryConfiguration.class.getSimpleName().equals(identifier);
    }

    @Override
    public Optional<ISnippetRepository> create(ISnippetRepositoryConfiguration configuration, File basedir) {
        ensureIsInstanceOf(configuration, EclipseGitSnippetRepositoryConfiguration.class);
        if (configuration instanceof EclipseGitSnippetRepositoryConfiguration) {
            EclipseGitSnippetRepositoryConfiguration config = cast(configuration);
            ISnippetRepository repo = new EclipseGitSnippetRepository(basedir, config.getRepositoryUrl(), bus);
            return of(repo);
        }
        return absent();
    }

    @Override
    public ISnippetRepositoryConfiguration fromPreferenceString(String stringRepresentation) {
        return gson.fromJson(stringRepresentation, EclipseGitSnippetRepositoryConfiguration.class);
    }

    @Override
    public String toPreferenceString(ISnippetRepositoryConfiguration configurations) {
        StringBuilder sb = new StringBuilder();
        sb.append(EclipseGitSnippetRepositoryConfiguration.class.getSimpleName());
        sb.append(RepositoryConfigurations.INNER_SEPARATOR);
        sb.append(gson.toJson(configurations));
        return sb.toString();
    }

    @Override
    public List<ISnippetRepositoryConfiguration> getDefaultConfigurations() {
        List<ISnippetRepositoryConfiguration> configurations = Lists.newArrayList();
        configurations.add(new EclipseGitSnippetRepositoryConfiguration(Messages.DEFAULT_REPO_NAME, REPO_URL, true));
        return configurations;
    }
}
