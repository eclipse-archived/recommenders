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

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.REPOSITORY_BASEDIR;
import static org.eclipse.recommenders.models.ModelCoordinate.HINT_REPOSITORY_URL;
import static org.eclipse.recommenders.utils.Urls.mangle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.recommenders.models.DownloadCallback;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryClosedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryOpenedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryUrlChangedEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * The Eclipse RCP wrapper around an {@link IModelRepository} that responds to (@link ModelRepositoryChangedEvent)s by
 * reconfiguring the underlying repository. It also manages proxy settings and handling of auto download properties.
 */
public class EclipseModelRepository implements IModelRepository, IRcpService {

    private static final Logger LOG = LoggerFactory.getLogger(EclipseModelRepository.class);

    private final File basedir;
    private final IProxyService proxy;
    private final ModelsRcpPreferences prefs;
    private final EventBus bus;
    private final List<Pair<String, ModelRepository>> delegates = Lists.newLinkedList();

    private boolean isOpen = false;

    @Inject
    public EclipseModelRepository(@Named(REPOSITORY_BASEDIR) File basedir, IProxyService proxy,
            ModelsRcpPreferences prefs, EventBus bus) {
        this.basedir = basedir;
        this.proxy = proxy;
        this.prefs = prefs;
        this.bus = bus;
    }

    @PostConstruct
    void open() throws Exception {
        String[] remoteUrls = prefs.remotes;
        List<Pair<String, ModelRepository>> repositories = Lists.newLinkedList();
        for (String remoteUrl : remoteUrls) {
            File cache = new File(basedir, mangle(remoteUrl));
            cache.mkdirs();
            repositories.add(Pair.newPair(remoteUrl, new ModelRepository(cache, remoteUrl)));
        }
        openInternal(repositories);
    }

    @VisibleForTesting
    void openInternal(List<Pair<String, ModelRepository>> repositories) {
        delegates.clear();
        delegates.addAll(repositories);
        isOpen = true;
        bus.post(new ModelRepositoryOpenedEvent());
    }

    @VisibleForTesting
    protected void close() {
        closeSilently();
        bus.post(new ModelRepositoryClosedEvent());
    }

    @PreDestroy
    void closeSilently() {
        isOpen = false;
    }

    @Subscribe
    public void onModelRepositoryChanged(ModelRepositoryUrlChangedEvent e) throws Exception {
        close();
        open();
    }

    private List<ModelRepository> searchDelegates(ModelCoordinate mc) {
        LinkedList<ModelRepository> result = Lists.newLinkedList();

        String repoUrl = mc.getHint(HINT_REPOSITORY_URL).orNull();
        for (Pair<String, ModelRepository> delegate : delegates) {
            if (repoUrl == null || repoUrl.equals(delegate.getFirst())) {
                result.add(delegate.getSecond());
            }
        }

        return result;
    }

    @Override
    public Optional<File> getLocation(final ModelCoordinate mc, boolean prefetch) {
        ensureIsOpen();

        List<ModelRepository> foundDelegates = searchDelegates(mc);
        for (ModelRepository delegate : foundDelegates) {
            Optional<File> location = delegate.getLocation(mc, false);
            if (prefetch && prefs.autoDownloadEnabled) {
                new DownloadModelArchiveJob(this, mc, false, bus).schedule();
            }
            if (location.isPresent()) {
                return location;
            }
        }
        return absent();
    }

    @Override
    public Optional<File> resolve(ModelCoordinate mc, boolean force) {
        ensureIsOpen();
        updateProxySettings();

        List<ModelRepository> foundSuitableDelegates = searchDelegates(mc);
        for (ModelRepository modelRepository : foundSuitableDelegates) {
            Optional<File> location = modelRepository.resolve(mc, force);
            if (location.isPresent()) {
                return location;
            }
        }
        return absent();
    }

    @Override
    public Optional<File> resolve(ModelCoordinate mc, boolean force, DownloadCallback callback) {
        ensureIsOpen();
        updateProxySettings();

        List<ModelRepository> foundSuitableDelegates = searchDelegates(mc);
        for (ModelRepository modelRepository : foundSuitableDelegates) {
            Optional<File> location = modelRepository.resolve(mc, force, callback);
            if (location.isPresent()) {
                return location;
            }
        }

        return absent();
    }

    private void updateProxySettings() {
        for (Pair<String, ModelRepository> delegate : delegates) {
            updateProxySettings(delegate.getFirst(), delegate.getSecond());
        }
    }

    private void updateProxySettings(String repositoryUri, ModelRepository modelRepository) {
        if (!proxy.isProxiesEnabled()) {
            modelRepository.unsetProxy();
            return;
        }
        try {
            URI uri = new URI(repositoryUri);
            if (uri.getScheme() == null) {
                return;
            }
            if (uri.getHost() == null) {
                return;
            }
            IProxyData[] entries = proxy.select(uri);
            if (entries.length == 0) {
                modelRepository.unsetProxy();
                return;
            }

            IProxyData proxyData = entries[0];
            String type = proxyData.getType().toLowerCase();
            String host = proxyData.getHost();
            int port = proxyData.getPort();
            String userId = proxyData.getUserId();
            String password = proxyData.getPassword();
            modelRepository.setProxy(type, host, port, userId, password);
        } catch (URISyntaxException e) {
            modelRepository.unsetProxy();
        }
    }

    public void deleteModels() throws IOException {
        try {
            close();
            FileUtils.cleanDirectory(basedir);
        } finally {
            try {
                open();
            } catch (Exception e) {
                LOG.error("A error occurred while opening EclipseModelRepository after deleting models.", e); //$NON-NLS-1$
            }
        }
    }

    private void ensureIsOpen() {
        Checks.ensureIsTrue(isOpen, "model repository service is not accesible at the moment."); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(delegates).toString();
    }
}
