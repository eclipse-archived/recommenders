/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.commons.lfm.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.ClasspathDependencyStoreLocation;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

@Singleton
public class ClasspathDependencyStore {

    private final File persistanceLocation;
    private final File dependencyInfoFile;
    private final File manifestIdFile;

    private final Map<File, ClasspathDependencyInformation> resource2dependencyInfo = Maps.newConcurrentMap();
    private final Map<File, Manifest> resource2manifestId = Maps.newConcurrentMap();

    @Inject
    public ClasspathDependencyStore(@ClasspathDependencyStoreLocation final File persistanceLocation) {
        this.persistanceLocation = persistanceLocation;
        dependencyInfoFile = new File(persistanceLocation, "dependencies.json");
        manifestIdFile = new File(persistanceLocation, "manifests.json");
        initialize();
    }

    private void initialize() {
        initializeMap(dependencyInfoFile, resource2dependencyInfo,
                new TypeToken<Map<File, ClasspathDependencyInformation>>() {
                });
        initializeMap(manifestIdFile, resource2manifestId, new TypeToken<Map<File, Manifest>>() {
        });
    }

    private <T> void initializeMap(final File f, final Map<File, T> map, final TypeToken<?> token) {
        if (f.exists()) {
            final Map<File, T> deserializedMap = GsonUtil.deserialize(f, token.getType());
            map.putAll(deserializedMap);
        }
    }

    public void store() {
        ensureIsNotNull(persistanceLocation,
                "ClasspathDependencyStore was initialized as in memory index. Storing not allowed!");
        persistanceLocation.mkdirs();
        GsonUtil.serialize(resource2dependencyInfo, dependencyInfoFile);
        GsonUtil.serialize(resource2manifestId, manifestIdFile);
    }

    public boolean containsClasspathDependencyInfo(final IPackageFragmentRoot packageRoot) {
        final File file = getLocation(packageRoot);
        final ClasspathDependencyInformation dependencyInformation = resource2dependencyInfo.get(file);
        if (dependencyInformation == null) {
            return false;
        }

        if (isFileChanged(file, dependencyInformation)) {
            resource2dependencyInfo.remove(file);
            return false;
        } else {
            return true;
        }
    }

    public boolean containsManifest(final IPackageFragmentRoot packageRoot) {
        final File file = getLocation(packageRoot);
        if (containsClasspathDependencyInfo(packageRoot)) {
            return resource2manifestId.containsKey(file);
        } else {
            resource2manifestId.remove(file);
            return false;
        }
    }

    public ClasspathDependencyInformation getClasspathDependencyInfo(final IPackageFragmentRoot packageRoot) {
        ensureIsTrue(containsClasspathDependencyInfo(packageRoot),
                "PackageRoot not contained  in mapping. Call containsClasspathDependencyInfo() before getClasspathDependencyInfo().");
        final File file = getLocation(packageRoot);
        return resource2dependencyInfo.get(file);
    }

    public Manifest getManifest(final IPackageFragmentRoot packageRoot) {
        ensureIsTrue(containsManifest(packageRoot),
                "PackageRoot not contained  in mapping. Call containsManifestIdentifier() before getManifestIdentifier().");
        final File file = getLocation(packageRoot);
        return resource2manifestId.get(file);
    }

    public void putClasspathDependencyInfo(final IPackageFragmentRoot packageRoot,
            final ClasspathDependencyInformation dependencyInformation) {
        final File file = getLocation(packageRoot);
        resource2dependencyInfo.put(file, dependencyInformation);
    }

    public void putManifest(final IPackageFragmentRoot packageRoot, final Manifest manifest) {
        final File file = getLocation(packageRoot);
        resource2manifestId.put(file, manifest);
    }

    public void invalidateManifest(final IPackageFragmentRoot packageFragmentRoot) {
        final File file = getLocation(packageFragmentRoot);
        resource2manifestId.remove(file);
    }

    private File getLocation(final IPackageFragmentRoot packageRoot) {
        final File location = packageRoot.getPath().toFile();
        return location;
    }

    private boolean isFileChanged(final File file, final ClasspathDependencyInformation dependencyInformation) {
        return file.lastModified() != dependencyInformation.jarFileModificationDate.getTime();
    }
}
