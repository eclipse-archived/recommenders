/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Johannes Dorn - Refactoring
 */
package org.eclipse.recommenders.internal.types.rcp;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class IndexProvider implements IIndexProvider {

    private final LoadingCache<IJavaProject, IProjectTypesIndex> cache = CacheBuilder.newBuilder().build(
            new ProjectTypesIndexCacheLoader());

    @Override
    public Optional<IProjectTypesIndex> findIndex(IJavaProject project) {
        return Optional.fromNullable(cache.getIfPresent(project));
    }

    @Override
    public IProjectTypesIndex findOrCreateIndex(IJavaProject project) {
        return cache.getUnchecked(project);
    }

    @Override
    public void close() throws IOException {
        for (IProjectTypesIndex index : cache.asMap().values()) {
            index.close();
        }
    }

    private static final class ProjectTypesIndexCacheLoader extends CacheLoader<IJavaProject, IProjectTypesIndex> {

        @Override
        public IProjectTypesIndex load(IJavaProject project) throws Exception {
            return new ProjectTypesIndex(project, computeIndexDir(project));
        }

        private static File computeIndexDir(IJavaProject project) {
            Bundle bundle = FrameworkUtil.getBundle(IndexProvider.class);
            File location = Platform.getStateLocation(bundle).toFile();
            String mangledProjectName = project.getElementName().replaceAll("\\W", "_"); //$NON-NLS-1$ //$NON-NLS-2$
            File indexDir = new File(new File(location, Constants.INDEX_DIR), mangledProjectName);
            return indexDir;
        }

    }

}
