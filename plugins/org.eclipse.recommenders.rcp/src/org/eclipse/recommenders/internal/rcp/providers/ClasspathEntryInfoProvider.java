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
package org.eclipse.recommenders.internal.rcp.providers;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.Executors.coreThreadsTimoutExecutor;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JarPackageFragmentRootAdded;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JavaProjectOpened;
import org.eclipse.recommenders.rcp.events.NewClasspathEntryFound;
import org.eclipse.recommenders.utils.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
@Singleton
public class ClasspathEntryInfoProvider implements IClasspathEntryInfoProvider {

    /**
     * Single-threaded executor used to compute fingerprints etc. for package fragment root. Single-threaded because
     * it's an disk-IO heavy computation. More than one thread will probably not give any performance gains here.
     */
    private final ExecutorService pool = coreThreadsTimoutExecutor(1, Thread.MIN_PRIORITY,
            "Recommenders-Dependency-Info-Service-");

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BiMap<File, ClasspathEntryInfo> cpeInfos = HashBiMap.create();
    private final File storageLocation;
    private final File infosFile;
    private final EventBus bus;

    @Inject
    public ClasspathEntryInfoProvider(File storeLocation, IWorkspaceRoot workspace, EventBus bus) {
        this.storageLocation = storeLocation;
        this.bus = bus;
        this.infosFile = new File(storeLocation, "archives.json");
        initialize();
        scanOpenProjects(workspace);
    }

    private void initialize() {
        initializeMap(infosFile, cpeInfos, new TypeToken<Map<File, ClasspathEntryInfo>>() {
        });

    }

    private void scanOpenProjects(IWorkspaceRoot workspace) {
        for (IProject p : workspace.getProjects()) {
            if (JavaProject.hasJavaNature(p)) {
                IJavaProject javaProject = JavaCore.create(p);
                onEvent(new JavaProjectOpened(javaProject));
            }
        }
    }

    private <T> void initializeMap(final File f, final Map<File, T> map, final TypeToken<?> token) {
        try {
            if (f.exists()) {
                final Map<File, T> deserializedMap = GsonUtil.deserialize(f, token.getType());
                if (deserializedMap != null) {
                    map.putAll(deserializedMap);
                }
                for (File archive : getFiles()) {
                    ensureFileInfosStillConsistent(archive);
                }
            }
        } catch (final Exception e) {
            log.error("Exception occurred during deserialization of cached package fragment root infos.", e);
        }
    }

    @Override
    public Optional<ClasspathEntryInfo> getInfo(final File file) {
        ensureFileInfosStillConsistent(file);
        return fromNullable(cpeInfos.get(file));
    }

    @Override
    public Set<File> getFiles() {
        return new HashSet<File>(cpeInfos.keySet());
    }

    private void ensureFileInfosStillConsistent(final File file) {
        final ClasspathEntryInfo cpeInfo = cpeInfos.get(file);
        if (cpeInfo == null) {
            return;
        } else if (hasFileChanged(file, cpeInfo)) {
            cpeInfos.remove(file);
        }
    }

    private boolean hasFileChanged(final File file, final ClasspathEntryInfo cpeInfo) {
        return file.lastModified() != cpeInfo.getModificationDate().getTime();
    }

    @Override
    public void close() {
        storageLocation.mkdirs();
        GsonUtil.serialize(cpeInfos, infosFile);
    }

    @Subscribe
    public void onEvent(final JavaProjectOpened e) {
        try {
            for (final IPackageFragmentRoot r : e.project.getAllPackageFragmentRoots()) {
                final Optional<File> location = JdtUtils.getLocation(r);
                if (isInterestingPackageFragmentRoot(r, location)) {
                    resolve(r, location.get());
                }
            }
        } catch (final JavaModelException x) {
            log.error("Exception occurred while resolving project dependencies for " + e.project, x);
        }
    }

    private void resolve(final IPackageFragmentRoot r, final File file) {
        pool.submit(new Runnable() {

            @Override
            public void run() {
                // if it has been resolved in the meanwhile, skip
                if (cpeInfos.containsKey(file)) {
                    return;
                }
                if (r.isArchive()) {
                    try {
                        final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(
                                file);
                        ClasspathEntryInfo res = new ClasspathEntryInfo();
                        res.setSymbolicName(extractor.extractName());
                        res.setVersion(extractor.extractVersion());
                        res.setFingerprint(extractor.createFingerprint());
                        res.setModificationDate(new Date(file.lastModified()));
                        cpeInfos.put(file, res);
                        bus.post(new NewClasspathEntryFound(r, file, res));
                    } catch (final Exception e) {
                        log.error("Extracing jar information failed with exception.", e);
                    }
                }
            }
        });
    }

    @Subscribe
    public void onEvent(final JarPackageFragmentRootAdded e) {
        final JarPackageFragmentRoot r = e.root;
        final Optional<File> location = JdtUtils.getLocation(r);
        if (isInterestingPackageFragmentRoot(r, location)) {
            resolve(r, location.get());
        }
    }

    private boolean isInterestingPackageFragmentRoot(final IPackageFragmentRoot r, final Optional<File> location) {
        return location.isPresent() && r.isArchive() && isNewLocation(location);
    }

    private boolean isNewLocation(final Optional<File> location) {
        return !cpeInfos.containsKey(location.get());
    }

}
