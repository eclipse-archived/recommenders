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

import java.io.File;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jdt.core.IPackageFragmentRoot;

import com.google.common.collect.Sets;

@Singleton
public class FragmentResolver {

    private final Set<File> inProgress = Sets.newHashSet();
    private final ClasspathDependencyStore dependencyStore;
    private final RemoteResolverJobFactory jobFactory;

    @Inject
    public FragmentResolver(final ClasspathDependencyStore dependencyStore, final RemoteResolverJobFactory jobFactory) {
        this.dependencyStore = dependencyStore;
        this.jobFactory = jobFactory;
    }

    public void resolve(final IPackageFragmentRoot[] packageFragmentRoots) {
        for (final IPackageFragmentRoot packageRoot : packageFragmentRoots) {
            final File file = getLocation(packageRoot);
            if (inProgress.contains(file)) {
                continue;
            }
            if (!dependencyStore.containsClasspathDependencyInfo(packageRoot)
                    || !dependencyStore.containsManifest(packageRoot)) {
                inProgress.add(file);
                scheduleJob(packageRoot);
            }
        }
    }

    private void scheduleJob(final IPackageFragmentRoot packageRoot) {
        final SearchManifestJob job = jobFactory.create(packageRoot);
        job.schedule();
    }

    private File getLocation(final IPackageFragmentRoot packageRoot) {
        final File location = packageRoot.getPath().toFile();
        return location;
    }

}
