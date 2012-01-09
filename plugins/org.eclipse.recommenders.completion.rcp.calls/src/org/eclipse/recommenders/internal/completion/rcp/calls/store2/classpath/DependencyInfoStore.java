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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.DependencyResolutionFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.DependencyResolutionRequested;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionRequested;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JavaProjectOpened;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.reflect.TypeToken;

public class DependencyInfoStore implements Closeable {

    private final BiMap<File, ClasspathDependencyInformation> dependencyInfos = HashBiMap.create();
    private final Map<File, ManifestResolverInfo> manifestInfos = Maps.newConcurrentMap();
    private final EventBus bus;
    private final File storageLocation;
    private final File dependenciesFile;
    private final File manifestsFile;

    public DependencyInfoStore(final File storeLocation, final EventBus bus) {
        this.bus = bus;
        this.storageLocation = storeLocation;
        this.dependenciesFile = new File(storeLocation, "dependencies.json");
        this.manifestsFile = new File(storeLocation, "manifests.json");
        initialize();
    }

    private void initialize() {
        initializeMap(dependenciesFile, dependencyInfos, new TypeToken<Map<File, ClasspathDependencyInformation>>() {
        });
        initializeMap(manifestsFile, manifestInfos, new TypeToken<Map<File, ManifestResolverInfo>>() {
        });
    }

    private <T> void initializeMap(final File f, final Map<File, T> map, final TypeToken<?> token) {
        try {
            if (f.exists()) {
                final Map<File, T> deserializedMap = GsonUtil.deserialize(f, token.getType());
                map.putAll(deserializedMap);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Optional<ClasspathDependencyInformation> getDependencyInfo(final File file) {
        ensureFileInfosStillConsistent(file);
        return fromNullable(dependencyInfos.get(file));
    }

    public Optional<ManifestResolverInfo> getManifestResolverInfo(final File file) {
        ensureFileInfosStillConsistent(file);
        ManifestResolverInfo info = manifestInfos.get(file);
        return fromNullable(info);
    }

    public Optional<Manifest> getManifest(final File file) {
        ensureFileInfosStillConsistent(file);
        Optional<ManifestResolverInfo> opt = getManifestResolverInfo(file);
        if (opt.isPresent()) {
            ManifestResolverInfo info = opt.get();
            return of(info.getManifest());
        }
        return absent();
    }

    public Set<File> getFiles() {
        return new HashSet<File>(dependencyInfos.keySet());
    }

    @Subscribe
    public void onEvent(final DependencyResolutionFinished e) {
        boolean isNewDependency = isNewDependencyInfo(e);
        registerDependency(e);
        if (isNewDependency) {
            requestManifestResolution(e);
        }
    }

    private void registerDependency(final DependencyResolutionFinished e) {
        dependencyInfos.put(e.fragmentLocation, e.dependency);
    }

    private void requestManifestResolution(final DependencyResolutionFinished e) {
        ManifestResolutionRequested request = new ManifestResolutionRequested();
        request.dependency = e.dependency;
        bus.post(request);
    }

    private void ensureFileInfosStillConsistent(final File file) {
        final ClasspathDependencyInformation info = dependencyInfos.get(file);
        if (info == null) {
            return;
        } else if (hasFileChanged(file, info)) {
            dependencyInfos.remove(file);
            manifestInfos.remove(file);
        }
    }

    private boolean hasFileChanged(final File file, final ClasspathDependencyInformation dependencyInformation) {
        return file.lastModified() != dependencyInformation.jarFileModificationDate.getTime();
    }

    private boolean isNewDependencyInfo(final DependencyResolutionFinished e) {
        return !dependencyInfos.containsValue(e.dependency);
    }

    @Subscribe
    public void onEvent(final ManifestResolutionFinished e) {
        registerManifest(e);
    }

    private void registerManifest(final ManifestResolutionFinished e) {
        File file = dependencyInfos.inverse().get(e.dependency);
        manifestInfos.put(file, e.manifestResolverInfo);
    }

    @Override
    public void close() throws IOException {
        storageLocation.mkdirs();
        GsonUtil.serialize(dependencyInfos, dependenciesFile);
        GsonUtil.serialize(manifestInfos, manifestsFile);
    }

    @Subscribe
    public void onEvent(final JavaProjectOpened e) {
        try {
            for (IPackageFragmentRoot r : e.project.getAllPackageFragmentRoots()) {
                Optional<File> location = JdtUtils.getLocation(r);
                if (isInterestingPackageFragmentRoot(r, location)) {
                    requestDependencyInfoResolution(r, location.get());
                }
            }
        } catch (JavaModelException x) {
            RecommendersPlugin.log(x);
        }
    }

    private boolean isInterestingPackageFragmentRoot(final IPackageFragmentRoot r, final Optional<File> location) {
        return location.isPresent() && r.isArchive() && isNewLocation(location);
    }

    private boolean isNewLocation(final Optional<File> location) {
        return !dependencyInfos.containsKey(location.get());
    }

    private void requestDependencyInfoResolution(final IPackageFragmentRoot r, final File file) {
        DependencyResolutionRequested e = new DependencyResolutionRequested();
        e.fragmentRoot = r;
        bus.post(e);
    }
}
