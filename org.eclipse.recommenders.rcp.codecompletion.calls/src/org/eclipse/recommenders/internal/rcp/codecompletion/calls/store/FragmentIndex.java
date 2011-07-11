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

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.commons.lfm.LibraryIdentifier;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.FragmentIndexFile;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

@Singleton
public class FragmentIndex {

    private static PackageFragmentRootConfiguration UNKNOWN = new PackageFragmentRootConfiguration(
            LibraryIdentifier.UNKNOWN, UpdatePolicy.NEVER);

    private final Map<File, PackageFragmentRootConfiguration> mapping = Maps.newConcurrentMap();

    private File file;

    public FragmentIndex() {
    }

    @Inject
    public FragmentIndex(@FragmentIndexFile final File file) {
        this.file = ensureIsNotNull(file);
        initialize(file);
    }

    private void initialize(final File file) {
        if (file.exists()) {
            final Map<File, PackageFragmentRootConfiguration> deserializedMap = GsonUtil.deserialize(file,
                    new TypeToken<Map<File, PackageFragmentRootConfiguration>>() {
                    }.getType());
            mapping.putAll(deserializedMap);
        }
    }

    public void store() {
        ensureIsNotNull(file, "FragmentIndex was initialized as in memory index. Storing not allowed!");
        GsonUtil.serialize(mapping, file);
    }

    public LibraryIdentifier getLibraryIdentifier(final IPackageFragmentRoot packageRoot) {
        return get(packageRoot).getLibraryIdentifier();
    }

    public UpdatePolicy getUpdatePolicy(final IPackageFragmentRoot packageRoot) {
        return get(packageRoot).getUpdatePolicy();
    }

    private PackageFragmentRootConfiguration get(final IPackageFragmentRoot packageRoot) {
        ensureIsNotNull(packageRoot);
        final File location = getLocation(packageRoot);
        final PackageFragmentRootConfiguration configuration = mapping.get(location);
        return configuration == null ? UNKNOWN : configuration;
    }

    public void put(final IPackageFragmentRoot packageRoot, final LibraryIdentifier libraryIdentifier) {
        ensureIsNotNull(packageRoot);
        ensureIsNotNull(libraryIdentifier);
        put(packageRoot, libraryIdentifier, UpdatePolicy.DEFAULT);
    }

    public void put(final IPackageFragmentRoot packageRoot, final LibraryIdentifier libraryIdentifier,
            final UpdatePolicy updatePolicy) {
        ensureIsNotNull(packageRoot);
        ensureIsNotNull(updatePolicy);
        final PackageFragmentRootConfiguration configuration = new PackageFragmentRootConfiguration(libraryIdentifier,
                updatePolicy);
        mapping.put(getLocation(packageRoot), configuration);
    }

    private File getLocation(final IPackageFragmentRoot packageRoot) {
        final File location = packageRoot.getPath().toFile();
        return location;
    }

    public boolean contains(final IPackageFragmentRoot packageRoot) {
        return mapping.containsKey(packageRoot);
    }
}
