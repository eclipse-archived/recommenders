/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.Executors.coreThreadsTimoutExecutor;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.getLocation;

import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.recommenders.commons.udc.DependencyInformation;
import org.eclipse.recommenders.internal.analysis.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.DependencyResolutionFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.DependencyResolutionRequested;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Singleton
@SuppressWarnings("restriction")
public class DependencyInfoComputerService {

    /**
     * Single-threaded executor used to compute fingerprints etc. for package
     * fragment root. Single-threaded because it's an disk-IO heavy computation.
     * More than one thread will probably not give any performance gains here.
     */
    private final ExecutorService pool = coreThreadsTimoutExecutor(1, Thread.MIN_PRIORITY,
            "Recommenders-Dependency-Info-Computer-");

    /**
     * Helper used to ignore {@link IPackageFragmentRoot}s that have been
     * requested already. Relies on
     * {@link JarPackageFragmentRoot#equals(Object)} to return true if two roots
     * point to the same jar.
     */
    private final Set<DependencyResolutionRequested> queue = Sets.newHashSet();

    private final EventBus bus;

    public DependencyInfoComputerService(final EventBus bus) {
        this.bus = bus;
    }

    @Subscribe
    public void onEvent(final DependencyResolutionRequested e) {
        if (!queue.contains(e)) {
            queue.add(e);
            scheduleRequest(e);
        }
    }

    private void scheduleRequest(final DependencyResolutionRequested e) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                resolveDependency(e);
            }
        });
    }

    private void resolveDependency(final DependencyResolutionRequested e) {
        try {
            final Optional<File> location = getLocation(e.fragmentRoot);
            if (!location.isPresent()) {
                return;
            }
            final File file = location.get();
            final Optional<DependencyInformation> info = computeDependencyInfo(file);
            if (info.isPresent()) {
                fireDone(e.fragmentRoot, info.get(), file);
            }

        } finally {
            queue.remove(e);
        }
    }

    private Optional<DependencyInformation> computeDependencyInfo(final File file) {
        DependencyInformation res = null;
        if (isJarFile(file)) {
            try {
                final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(file);
                res = new DependencyInformation();
                res.symbolicName = extractor.extractName();
                res.version = extractor.extractVersion();
                res.jarFileFingerprint = extractor.createFingerprint();
                res.jarFileModificationDate = new Date(file.lastModified());
            } catch (final Exception e) {
                RecommendersPlugin.logError(e, "Extracing jar information failed wiht exception.");
            }
        }
        return fromNullable(res);
    }

    private boolean isJarFile(final File file) {
        return file.getName().endsWith(".jar");
    }

    protected void fireDone(final IPackageFragmentRoot fragmentRoot, final DependencyInformation dependency,
            final File fileLocation) {
        final DependencyResolutionFinished event = new DependencyResolutionFinished();
        event.fragmentLocation = fileLocation;
        event.dependency = dependency;
        bus.post(event);
    }
}
