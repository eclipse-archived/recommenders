/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.completion.rcp.overrides.OverridesModule.OverridesModelStore;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.archive.PoolingModelArchive;
import org.eclipse.recommenders.internal.rcp.models.store.DefaultModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.store.IDependenciesFactory;
import org.eclipse.recommenders.internal.rcp.models.store.ModelArchiveResolutionJob;
import org.eclipse.recommenders.internal.rcp.wiring.RecommendersModule.AutoCloseOnWorkbenchShutdown;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ManualModelStoreWiring {
    @AutoCloseOnWorkbenchShutdown
    static class OverridesModelArchiveStore extends DefaultModelArchiveStore<IType, ClassOverridesNetwork> {

        private static final String CLASSIFIER = "ovrm";

        @Inject
        public OverridesModelArchiveStore(@OverridesModelStore File store, final IModelRepository repository,
                final IModelRepositoryIndex searchindex, final IClasspathEntryInfoProvider cpeInfoProvider,
                final JavaElementResolver jdtResolver) {
            super(store, CLASSIFIER, repository, new IDependenciesFactory() {

                @Override
                public ModelArchiveResolutionJob newResolutionJob(ModelArchiveMetadata metadata, String classifier) {
                    return new ModelArchiveResolutionJob(metadata, cpeInfoProvider, repository, searchindex, CLASSIFIER);
                }

                @Override
                public IModelArchive<IType, ClassOverridesNetwork> newModelArchive(File location) throws IOException {
                    if (!location.exists()) {
                        throw new FileNotFoundException("Model file not found: " + location.getAbsolutePath());
                    }
                    return new PoolingModelArchive(new OverridesZipModelFactory(location, jdtResolver));
                }
            });
        }
    }
}