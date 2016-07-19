/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - multi index support.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Optional.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.INDEX_BASEDIR;
import static org.eclipse.recommenders.internal.models.rcp.l10n.LogMessages.*;
import static org.eclipse.recommenders.models.ModelCoordinate.HINT_REPOSITORY_URL;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.models.rcp.l10n.LogMessages;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelIndex;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelArchiveDownloadedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryClosedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryOpenedEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.Uris;
import org.eclipse.recommenders.utils.Zips;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.common.util.concurrent.AbstractIdleService;

/**
 * The Eclipse RCP wrapper around an IModelIndex that responds to (@link ModelRepositoryChangedEvent)s by closing the
 * underlying, downloading the new index if required and reopening the index.
 */
public class EclipseModelIndex extends AbstractIdleService implements IModelIndex, IRcpService {

    private static final int CACHE_SIZE = 10;

    private final File basedir;
    private final ModelsRcpPreferences prefs;
    private final IModelRepository repository;
    private final EventBus bus;

    /*
     * Contains only open indices: An IModelIndex will only be added after it is opened and removed before it is closed.
     * For all read access openDelegates provides a consistent version.
     */
    private volatile ImmutableMap<String, Pair<File, IModelIndex>> openDelegates;

    private final Cache<Pair<ProjectCoordinate, String>, Optional<ModelCoordinate>> cache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE).concurrencyLevel(1).build();

    @Inject
    public EclipseModelIndex(@Named(INDEX_BASEDIR) File basedir, ModelsRcpPreferences prefs,
            IModelRepository repository, EventBus bus) {
        this.basedir = basedir;
        this.prefs = prefs;
        this.repository = repository;
        this.bus = bus;
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        stopAsync();
        try {
            awaitTerminated(5, SECONDS);
        } catch (TimeoutException e) {
            log(ERROR_CLOSING_MODEL_INDEX_SERVICE, e);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        cache.invalidateAll();
        for (Pair<File, IModelIndex> delegate : openDelegates.values()) {
            removeDelegate(delegate);
            delegate.getSecond().close();
        }
    }

    @PostConstruct
    @Override
    public void open() throws IOException {
        startAsync();
    }

    @Override
    protected void startUp() throws Exception {
        Checks.ensureNoDuplicates(prefs.remotes);
        clearDelegates();
        basedir.mkdir();
        for (String remoteUri : prefs.remotes) {
            File file = createIndexLocation(remoteUri);
            if (indexAlreadyDownloaded(file)) {
                openDelegate(remoteUri, file);
            }
            triggerIndexDownload(remoteUri);
        }
    }

    @VisibleForTesting
    public void openForTesting() throws IOException {
        Checks.ensureNoDuplicates(prefs.remotes);
        clearDelegates();
        for (String remoteUri : prefs.remotes) {
            File file = createIndexLocation(remoteUri);
            openDelegate(remoteUri, file);
        }
    }

    private synchronized void storeDelegate(String remoteUrl, Pair<File, IModelIndex> pair) {
        openDelegates = new ImmutableMap.Builder<String, Pair<File, IModelIndex>>().putAll(openDelegates)
                .put(remoteUrl, pair).build();
    }

    private synchronized void removeDelegate(Pair<File, IModelIndex> delegate) {
        HashMap<String, Pair<File, IModelIndex>> delegates = Maps.newHashMap(openDelegates);
        delegates.values().remove(delegate);
        openDelegates = new ImmutableMap.Builder<String, Pair<File, IModelIndex>>().putAll(delegates).build();
    }

    private synchronized void clearDelegates() {
        openDelegates = ImmutableMap.of();
    }

    private void openDelegate(String remoteUrl, File indexLocation) throws IOException {
        IModelIndex modelIndex = createModelIndex(indexLocation);
        modelIndex.open();
        storeDelegate(remoteUrl, Pair.newPair(indexLocation, modelIndex));
        bus.post(new ModelIndexOpenedEvent());
    }

    private File createIndexLocation(String remoteUri) {
        return new File(basedir, Uris.mangle(Uris.toUri(remoteUri)));
    }

    @VisibleForTesting
    protected IModelIndex createModelIndex(File indexLocation) {
        return new ModelIndex(indexLocation);
    }

    private void triggerIndexDownload(String remoteUrl) {
        ModelCoordinate mc = createIndexCoordinateWithRemoteUrlHint(remoteUrl);
        new DownloadModelArchiveJob(repository, mc, true, bus).schedule(300);
    }

    private ModelCoordinate createIndexCoordinateWithRemoteUrlHint(String remoteUrl) {
        ModelCoordinate mc = new ModelCoordinate(INDEX.getGroupId(), INDEX.getArtifactId(), INDEX.getClassifier(),
                INDEX.getExtension(), INDEX.getVersion());
        return createCopyWithRepositoryUrlHint(mc, remoteUrl);
    }

    private boolean indexAlreadyDownloaded(File location) {
        if (!location.exists()) {
            return false;
        }
        File[] files = location.listFiles();
        if (files == null) {
            return false;
        }
        if (files.length <= 1) {
            // If this folder contains an index, there must be more than one file...
            // TODO However, on Mac OS, we often have hidden files in the folder. This is just simple heuristic.
            return false;
        }
        return true;
    }

    /**
     * This implementation caches the previous results
     */
    @Override
    public Optional<ModelCoordinate> suggest(final ProjectCoordinate pc, final String modelType) {
        if (!isRunning()) {
            log(INFO_SERVICE_NOT_RUNNING);
            return absent();
        }
        Pair<ProjectCoordinate, String> key = Pair.newPair(pc, modelType);
        try {
            return cache.get(key, new Callable<Optional<ModelCoordinate>>() {

                @Override
                public Optional<ModelCoordinate> call() {
                    for (String remote : prefs.remotes) {
                        Pair<File, IModelIndex> pair = openDelegates.get(remote);
                        if (pair == null) {
                            continue; // Index not (yet) available; try next remote repository
                        }
                        IModelIndex index = pair.getSecond();
                        Optional<ModelCoordinate> suggest = index.suggest(pc, modelType);
                        if (suggest.isPresent()) {
                            return of(createCopyWithRepositoryUrlHint(suggest.get(), remote));
                        }
                    }
                    return absent();
                }
            });
        } catch (ExecutionException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_ACCESS_MODEL_COORDINATES_CACHE, e);
            return absent();
        }
    }

    @Override
    public ImmutableSet<ModelCoordinate> suggestCandidates(ProjectCoordinate pc, String modelType) {
        if (!isRunning()) {
            log(INFO_SERVICE_NOT_RUNNING);
            return ImmutableSet.of();
        }
        Set<ModelCoordinate> candidates = Sets.newHashSet();
        for (Entry<String, Pair<File, IModelIndex>> entry : openDelegates.entrySet()) {
            IModelIndex index = entry.getValue().getSecond();
            candidates.addAll(addRepositoryUrlHint(index.suggestCandidates(pc, modelType), entry.getKey()));
        }

        return ImmutableSet.copyOf(candidates);
    }

    @Override
    public ImmutableSet<ModelCoordinate> getKnownModels(String modelType) {
        if (!isRunning()) {
            log(INFO_SERVICE_NOT_RUNNING);
            return ImmutableSet.of();
        }
        Set<ModelCoordinate> models = Sets.newHashSet();
        for (Entry<String, Pair<File, IModelIndex>> entry : openDelegates.entrySet()) {
            IModelIndex index = entry.getValue().getSecond();
            models.addAll(addRepositoryUrlHint(index.getKnownModels(modelType), entry.getKey()));
        }

        return ImmutableSet.copyOf(models);
    }

    @Override
    public Optional<ProjectCoordinate> suggestProjectCoordinateByArtifactId(String artifactId) {
        if (!isRunning()) {
            log(INFO_SERVICE_NOT_RUNNING);
            return absent();
        }
        for (Pair<File, IModelIndex> delegate : openDelegates.values()) {
            IModelIndex index = delegate.getSecond();
            Optional<ProjectCoordinate> suggest = index.suggestProjectCoordinateByArtifactId(artifactId);
            if (suggest.isPresent()) {
                return suggest;
            }
        }

        return absent();
    }

    @Override
    public Optional<ProjectCoordinate> suggestProjectCoordinateByFingerprint(String fingerprint) {
        if (!isRunning()) {
            log(INFO_SERVICE_NOT_RUNNING);
            return absent();
        }
        for (Pair<File, IModelIndex> delegate : openDelegates.values()) {
            IModelIndex index = delegate.getSecond();
            Optional<ProjectCoordinate> suggest = index.suggestProjectCoordinateByFingerprint(fingerprint);
            if (suggest.isPresent()) {
                return suggest;
            }
        }

        return absent();
    }

    @Subscribe
    public void onEvent(ModelRepositoryOpenedEvent e) throws Exception {
        if (!isRunning()) {
            // Log this to see whether my expectations are correct
            log(INFO_SERVICE_NOT_RUNNING);
        }
        startUp();
    }

    @Subscribe
    public void onEvent(ModelIndexOpenedEvent e) {
        // We don't check whether the service is running here, because this event is fired during opening.
        // When the model index is finally opened, invalidate the cache, as we may have cached a "not found" for
        // something that can be found in the newly opened index.
        cache.invalidateAll();
    }

    @Subscribe
    public void onEvent(ModelRepositoryClosedEvent e) throws Exception {
        if (!isRunning()) {
            // Log this to see whether my expectations are correct
            log(INFO_SERVICE_NOT_RUNNING);
        }
        // XXX: this is closing the repo but not setting the service state to stopped.
        shutDown();
    }

    @Subscribe
    public void onEvent(ModelArchiveDownloadedEvent e) throws IOException {
        if (!isRunning()) {
            log(INFO_SERVICE_NOT_RUNNING);
            return;
        }
        if (isIndex(e.model)) {
            File location = repository.getLocation(e.model, false).orNull();
            String remoteUri = e.model.getHint(HINT_REPOSITORY_URL).orNull();
            if (remoteUri != null) {
                Pair<File, IModelIndex> pair = openDelegates.get(remoteUri);
                if (pair == null) {
                    File folder = createIndexLocation(remoteUri);
                    folder.mkdir();
                    Zips.unzip(location, folder);
                    openDelegate(remoteUri, folder);
                } else {
                    File folder = Files.createTempDir();
                    Zips.unzip(location, folder);
                    IModelIndex modelIndex = pair.getSecond();
                    modelIndex.updateIndex(folder);
                    bus.post(new ModelIndexOpenedEvent());
                    FileUtils.deleteDirectory(folder);
                }
            }
        }
    }

    private boolean isIndex(ModelCoordinate model) {
        return model.getGroupId().equals(INDEX.getGroupId()) && model.getArtifactId().equals(INDEX.getArtifactId())
                && model.getExtension().equals(INDEX.getExtension()) && model.getVersion().equals(INDEX.getVersion());
    }

    @Override
    public void updateIndex(File index) throws IOException {
        throw new UnsupportedOperationException();
    }

    public static Set<ModelCoordinate> addRepositoryUrlHint(Set<ModelCoordinate> modelCoordinates, String url) {
        Set<ModelCoordinate> modelCoordinatesWithUrlHint = Sets.newHashSet();
        for (ModelCoordinate modelCoordinate : modelCoordinates) {
            modelCoordinatesWithUrlHint.add(createCopyWithRepositoryUrlHint(modelCoordinate, url));
        }
        return modelCoordinatesWithUrlHint;
    }

    private static ModelCoordinate createCopyWithRepositoryUrlHint(ModelCoordinate mc, String url) {
        Map<String, String> hints = Maps.newHashMap(mc.getHints());
        hints.put(ModelCoordinate.HINT_REPOSITORY_URL, url);
        ModelCoordinate copy = new ModelCoordinate(mc.getGroupId(), mc.getArtifactId(), mc.getClassifier(),
                mc.getExtension(), mc.getVersion(), hints);
        return copy;
    }
}
