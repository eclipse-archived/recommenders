/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.LocalModelRepositoryLocation;
import org.eclipse.recommenders.models.AetherModelRepository;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.Pair;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class EclipseModelRepository implements IModelRepository, IRcpService {

    @Inject
    @LocalModelRepositoryLocation
    File repodir;

    @Inject
    IProxyService proxy;

    @Inject
    ModelsRcpPreferences prefs;

    Cache<Pair<ProjectCoordinate, String>, Optional<ModelArchiveCoordinate>> cache = CacheBuilder.newBuilder()
            .maximumSize(10).concurrencyLevel(1).build();

    AetherModelRepository delegate;

    @PostConstruct
    public void open() throws Exception {
        delegate = new AetherModelRepository(repodir.getParentFile(), prefs.remote);
        delegate.open();

        // XXX that's not great but there is yet no way to mix Guice with E4 and we need a callback when the URL changes
        prefs.addRemoteUrlChangedCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    AetherModelRepository tmp = new AetherModelRepository(repodir.getParentFile(), prefs.remote);
                    // triggers index download and the like
                    tmp.open();
                    // XXX this cannot work well in reality when urls are actually changed...
                    delegate = tmp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void resolve(ModelArchiveCoordinate model) throws Exception {
        if (prefs.autoDownloadEnabled) {
            updateProxySettings();
            delegate.resolve(model);
        }
    }

    private void updateProxySettings() {
        if (!proxy.isProxiesEnabled()) {
            delegate.unsetProxy();
            return;
        }
        try {
            URI uri = new URI(delegate.getRemoteUrl());
            IProxyData[] entries = proxy.select(uri);
            if (entries.length == 0) {
                delegate.unsetProxy();
                return;
            }

            IProxyData proxyData = entries[0];
            String type = proxyData.getType().toLowerCase();
            String host = proxyData.getHost();
            int port = proxyData.getPort();
            String userId = proxyData.getUserId();
            String password = proxyData.getPassword();
            delegate.setProxy(type, host, port, userId, password);
        } catch (URISyntaxException e) {
            delegate.unsetProxy();
        }
    }

    @Override
    public Optional<File> getLocation(ModelArchiveCoordinate coordinate) {
        return delegate.getLocation(coordinate);
    }

    @Override
    public Optional<ModelArchiveCoordinate> findBestModelArchive(final ProjectCoordinate coordinate,
            final String modelType) {
        try {
            return cache.get(Pair.newPair(coordinate, modelType), new Callable<Optional<ModelArchiveCoordinate>>() {

                @Override
                public Optional<ModelArchiveCoordinate> call() throws Exception {
                    return delegate.findBestModelArchive(coordinate, modelType);
                }
            });
        } catch (ExecutionException e) {
            return Optional.absent();
        }
    }

    @Override
    public Collection<ModelArchiveCoordinate> listModels(String classifier) {
        return delegate.listModels(classifier);
    }
}
