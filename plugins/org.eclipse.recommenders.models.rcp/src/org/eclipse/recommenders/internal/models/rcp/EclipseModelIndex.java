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
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.INDEX_BASEDIR;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelIndex;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelArchiveDownloadedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryClosedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryOpenedEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.Urls;
import org.eclipse.recommenders.utils.Zips;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * The Eclipse RCP wrapper around an IModelIndex that responds to (@link ModelRepositoryChangedEvent)s by closing the
 * underlying, downloading the new index if required and reopening the index.
 */
public class EclipseModelIndex implements IModelIndex, IRcpService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    @Named(INDEX_BASEDIR)
    File basedir;

    @Inject
    ModelsRcpPreferences prefs;

    @Inject
    IModelRepository repository;

    @Inject
    EventBus bus;

    private ModelIndex delegate;

    private Cache<Pair<ProjectCoordinate, String>, Optional<ModelCoordinate>> cache = CacheBuilder.newBuilder()
            .maximumSize(10).concurrencyLevel(1).build();

    private File activedir;

    @PostConstruct
    @Override
    public void open() throws IOException {
        doOpen(true);
    }

    private void doOpen(boolean scheduleIndexUpdate) throws IOException {
        activedir = new File(basedir, Urls.mangle(prefs.remote));
        delegate = new ModelIndex(activedir);
        if (!indexAlreadyDownloaded(activedir)) {
            new DownloadModelArchiveJob(repository, INDEX, true, bus).schedule(300);
            return;
        }
        delegate.open();
        bus.post(new ModelIndexOpenedEvent());
        if (scheduleIndexUpdate) {
            new DownloadModelArchiveJob(repository, INDEX, true, bus).schedule(300);
        }
    }

    private boolean indexAlreadyDownloaded(File location) {
        return location.exists() && location.listFiles().length > 1;
        // 2 = if this folder contains an index, there must be more than one file...
        // On mac, we often have hidden files in the folder. This is just simple heuristic.
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        cache.invalidateAll();
        delegate.close();
    }

    /**
     * This implementation caches the previous results
     */
    @Override
    public Optional<ModelCoordinate> suggest(final ProjectCoordinate pc, final String modelType) {
        Pair<ProjectCoordinate, String> key = Pair.newPair(pc, modelType);
        try {
            return cache.get(key, new Callable<Optional<ModelCoordinate>>() {

                @Override
                public Optional<ModelCoordinate> call() {
                    return delegate.suggest(pc, modelType);
                }
            });
        } catch (ExecutionException e) {
            log.error("Exception occured while accessing model coordinates cache", e);
            return absent();
        }
    }

    @Override
    public ImmutableSet<ModelCoordinate> suggestCandidates(ProjectCoordinate pc, String modelType) {
        return delegate.suggestCandidates(pc, modelType);
    }

    @Override
    public ImmutableSet<ModelCoordinate> getKnownModels(String modelType) {
        return delegate.getKnownModels(modelType);
    }

    @Override
    public Optional<ProjectCoordinate> suggestProjectCoordinateByArtifactId(String artifactId) {
        return delegate.suggestProjectCoordinateByArtifactId(artifactId);
    }

    @Override
    public Optional<ProjectCoordinate> suggestProjectCoordinateByFingerprint(String fingerprint) {
        return delegate.suggestProjectCoordinateByFingerprint(fingerprint);
    }

    @Subscribe
    public void onEvent(ModelRepositoryOpenedEvent e) throws IOException {
        doOpen(true);
    }

    @Subscribe
    public void onEvent(ModelRepositoryClosedEvent e) throws IOException {
        close();
    }

    @Subscribe
    public void onEvent(ModelArchiveDownloadedEvent e) throws IOException {
        if (INDEX.equals(e.model)) {
            close();
            File location = repository.getLocation(INDEX, false).orNull();
            activedir.mkdirs();
            FileUtils.cleanDirectory(activedir);
            Zips.unzip(location, activedir);
            doOpen(false);
        }
    }
}
