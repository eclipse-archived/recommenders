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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
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
    private final Map<File, ManifestResolvementInformation> resource2manifestInfo = Maps.newConcurrentMap();

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
        initializeMap(manifestIdFile, resource2manifestInfo,
                new TypeToken<Map<File, ManifestResolvementInformation>>() {
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
        GsonUtil.serialize(resource2manifestInfo, manifestIdFile);
    }

    public boolean containsClasspathDependencyInfo(final File file) {
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

    public boolean containsManifest(final File file) {
        if (containsClasspathDependencyInfo(file)) {
            return resource2manifestInfo.containsKey(file);
        } else {
            resource2manifestInfo.remove(file);
            return false;
        }
    }

    public ClasspathDependencyInformation getClasspathDependencyInfo(final File file) {
        ensureIsTrue(containsClasspathDependencyInfo(file),
                "File not contained in mapping. Call containsClasspathDependencyInfo() before getClasspathDependencyInfo().");
        return resource2dependencyInfo.get(file);
    }

    public Manifest getManifest(final File file) {
        return getManifestResolvementInfo(file).getManifest();
    }

    public ManifestResolvementInformation getManifestResolvementInfo(final File file) {
        ensureIsTrue(containsManifest(file),
                "File not contained in mapping. Call containsManifestIdentifier() before getManifestResolvementInfo().");
        return resource2manifestInfo.get(file);
    }

    public void putClasspathDependencyInfo(final File file, final ClasspathDependencyInformation dependencyInformation) {
        resource2dependencyInfo.put(file, dependencyInformation);
    }

    public void putManifest(final File file, final Manifest manifest) {
        putManifest(file, manifest, false);
    }

    public void putManifest(final File file, final Manifest manifest, final boolean manualResolved) {
        final ManifestResolvementInformation resolvementInfo = new ManifestResolvementInformation(manifest,
                manualResolved);
        resource2manifestInfo.put(file, resolvementInfo);
    }

    public void invalidateClasspathDependencyInfo(final File file) {
        resource2dependencyInfo.remove(file);
        invalidateManifest(file);
    }

    public void invalidateManifest(final File file) {
        resource2manifestInfo.remove(file);
    }

    private boolean isFileChanged(final File file, final ClasspathDependencyInformation dependencyInformation) {
        return file.lastModified() != dependencyInformation.jarFileModificationDate.getTime();
    }

    public Set<File> getFiles() {
        return new HashSet<File>(resource2dependencyInfo.keySet());
    }

}
