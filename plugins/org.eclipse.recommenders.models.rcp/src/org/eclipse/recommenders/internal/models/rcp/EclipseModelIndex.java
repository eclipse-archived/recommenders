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
import static org.eclipse.osgi.util.NLS.bind;
import static org.eclipse.recommenders.internal.models.rcp.Constants.BUNDLE_ID;
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.INDEX_BASEDIR;
import static org.eclipse.ui.internal.misc.StatusUtil.newStatus;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelIndex;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryUrlChangedEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.Urls;
import org.eclipse.recommenders.utils.Zips;

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
@SuppressWarnings("restriction")
public class EclipseModelIndex implements IModelIndex, IRcpService {

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

    @PostConstruct
    @Override
    public void open() throws IOException {
        final File indexdir = new File(basedir, Urls.mangle(prefs.remote));
        delegate = new ModelIndex(indexdir);
        // TODO how do we trigger "is index updated?" lookups?
        if (indexAlreadyDownloaded(indexdir)) {
            delegate.open();
            bus.post(new ModelIndexOpenedEvent());
            // TODO does it make sense to inform views etc. that there is a model index available now?
            // views, e.g, could refresh automatically with such an event
        } else {
            // we schedule with a small delay to make sure that the model repository already switched
            // to the new url
            new DownloadModelIndexJob(repository, INDEX, indexdir).schedule(300);
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
            // TODO log this exception
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
    public void onEvent(ModelRepositoryUrlChangedEvent e) throws IOException {
        close();
        open();
    }

    @Subscribe
    public void onEvent(ModelIndexOpenedEvent e) {
        // when a new model index is available, all old cached entries are invalid
        cache.invalidateAll();
    }

    private final class DownloadModelIndexJob extends DownloadModelArchiveJob {
        private final File indexdir;

        private DownloadModelIndexJob(IModelRepository repository, ModelCoordinate mc, File indexdir) {
            super(repository, mc);
            this.indexdir = indexdir;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            IStatus res = super.run(monitor);
            if (!res.isOK()) {
                return res;
            }
            try {
                initalizeIndex(indexdir);
            } catch (Exception e) {
                res = newStatus(BUNDLE_ID, bind(Messages.JOB_UPDATE_MODEL_INDEX, indexdir), e);
            }
            return res;
        }

        private void initalizeIndex(final File indexdir) throws IOException {
            File location = repository.getLocation(INDEX).orNull();
            indexdir.mkdirs();
            Zips.unzip(location, indexdir);
            delegate.open();
            bus.post(new ModelIndexOpenedEvent());
        }
    }

}
