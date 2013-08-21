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

import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.REPOSITORY_BASEDIR;
import static org.eclipse.recommenders.utils.Urls.mangle;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.recommenders.models.DownloadCallback;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryUrlChangedEvent;
import org.eclipse.recommenders.rcp.IRcpService;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * The Eclipse RCP wrapper around an {@link IModelRepository} that responds to (@link ModelRepositoryChangedEvent)s by
 * reconfiguring the underlying repository. It also manages proxy settings and handling of auto download properties.
 */
public class EclipseModelRepository implements IModelRepository, IRcpService {

    @Inject
    @Named(REPOSITORY_BASEDIR)
    File basedir;

    @Inject
    IProxyService proxy;

    @Inject
    ModelsRcpPreferences prefs;

    ModelRepository delegate;

    @PostConstruct
    void open() throws Exception {
        File cache = new File(basedir, mangle(prefs.remote));
        cache.mkdirs();
        delegate = new ModelRepository(cache, prefs.remote);
    }

    @PreDestroy
    void close() throws Exception {
    }

    @Subscribe
    public void onModelRepositoryChanged(ModelRepositoryUrlChangedEvent e) throws Exception {
        close();
        open();
    }

    @Override
    public Optional<File> resolve(ModelCoordinate mc) throws Exception {
        updateProxySettings();
        return delegate.resolve(mc);
    }

    public boolean isDownloaded(final ModelCoordinate mc) {
        return delegate.getLocation(mc).isPresent();
    }

    @Override
    public Optional<File> getLocation(final ModelCoordinate mc) {
        Optional<File> location = delegate.getLocation(mc);
        if (!location.isPresent() && prefs.autoDownloadEnabled) {
            updateProxySettings();
            new DownloadModelArchiveJob(delegate, mc).schedule();
        }
        return location;
    }

    @Override
    public ListenableFuture<File> resolve(ModelCoordinate mc, DownloadCallback callback) {
        updateProxySettings();
        return delegate.resolve(mc, callback);
    }

    void updateProxySettings() {
        if (!proxy.isProxiesEnabled()) {
            delegate.unsetProxy();
            return;
        }
        try {
            URI uri = new URI(prefs.remote);
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

}
