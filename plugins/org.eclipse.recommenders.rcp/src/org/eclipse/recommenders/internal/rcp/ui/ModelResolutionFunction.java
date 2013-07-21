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
package org.eclipse.recommenders.internal.rcp.ui;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.Version;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ModelResolutionFunction {

    @Inject
    IClasspathEntryInfoProvider cpeProvider;
    @Inject
    IModelRepositoryIndex index;
    @Inject
    IModelRepository repo;

    LoadingCache<Pair<ClasspathEntryInfo, String>, Artifact> modelBaseToBest = CacheBuilder.newBuilder().weakKeys()
            .build(new CacheLoader<Pair<ClasspathEntryInfo, String>, Artifact>() {

                @Override
                public Artifact load(Pair<ClasspathEntryInfo, String> key) throws Exception {
                    Artifact base = findModelBaseCoordinate(key.getFirst(), key.getSecond());
                    return findBestMatchingLatestModel(base, key.getFirst().getVersion());

                }
            });

    public Optional<Artifact> resolve(File cp, String classifier) {
        ClasspathEntryInfo info = cpeProvider.getInfo(cp).orNull();
        if (info == null) {
            return Optional.absent();
        }
        try {
            return Optional.of(modelBaseToBest.get(Pair.newPair(info, classifier)));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }

    private Artifact findModelBaseCoordinate(ClasspathEntryInfo info, String classifier) {
        if (!isEmpty(info.getFingerprint())) {
            Optional<Artifact> opt = index.searchByFingerprint(info.getFingerprint(), classifier);
            if (opt.isPresent()) {
                return findBestMatchingLatestModel(opt.get(), info.getVersion());
            }
        }

        if (!isEmpty(info.getSymbolicName())) {
            Optional<Artifact> opt = index.searchByArtifactId(info.getSymbolicName(), classifier);
            if (opt.isPresent()) {
                return findBestMatchingLatestModel(opt.get(), info.getVersion());
            }
        }
        return RepositoryUtils.newArtifact("unkown:unknown:" + classifier + "0.0.0");

    }

    private Artifact findBestMatchingLatestModel(Artifact base, Version version) {
        String upperBound = version.isUnknown() ? "10000.0" : format("%d.%d", version.major, version.minor + 1); //$NON-NLS-1$ //$NON-NLS-2$
        Artifact queryBestLowerMatch = base.setVersion("[0," + upperBound + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        Artifact queryBestUpperMatch = base.setVersion("[" + upperBound + ",)"); //$NON-NLS-1$ //$NON-NLS-2$
        Artifact match = repo.findHigestVersion(queryBestLowerMatch).orNull();
        if (match == null) {
            match = repo.findLowestVersion(queryBestUpperMatch).orNull();
        }
        return match != null ? match : base;
    }

}
