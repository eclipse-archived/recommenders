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
package org.eclipse.recommenders.internal.calls.rcp.store;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    public void resolve(final File[] files) {
        for (final File file : files) {
            if (inProgress.contains(file) || !isJarFile(file)) {
                continue;
            }
            if (!dependencyStore.containsClasspathDependencyInfo(file)) {
                inProgress.add(file);
                scheduleJob(file);
            }
        }
    }

    private void scheduleJob(final File file) {
        final ResolveCallsModelJob job = jobFactory.create(file, CallsModelResolver.OverridePolicy.NONE);
        job.schedule();
    }

    private boolean isJarFile(final File file) {
        return file.getName().endsWith(".jar");
    }

}
