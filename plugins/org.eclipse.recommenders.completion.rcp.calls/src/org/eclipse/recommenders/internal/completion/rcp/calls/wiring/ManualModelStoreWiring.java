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
package org.eclipse.recommenders.internal.completion.rcp.calls.wiring;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallNetZipModelFactory;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionModule.CallModelStore;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.archive.PoolingModelArchive;
import org.eclipse.recommenders.internal.rcp.models.store.DefaultModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.store.IDependenciesFactory;
import org.eclipse.recommenders.internal.rcp.models.store.ModelArchiveResolutionJob;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ManualModelStoreWiring {

    static class CallModelArchiveStore extends DefaultModelArchiveStore<IType, IObjectMethodCallsNet> {

        @Inject
        public CallModelArchiveStore(@CallModelStore File store, final IModelRepository repository,
                final IModelRepositoryIndex searchindex, final IClasspathEntryInfoProvider cpeInfoProvider,
                final JavaElementResolver jdtResolver) {
            super(store, "call", repository, new IDependenciesFactory() {

                @Override
                public ModelArchiveResolutionJob newResolutionJob(ModelArchiveMetadata metadata, String classifier) {
                    return new ModelArchiveResolutionJob(metadata, cpeInfoProvider, repository, searchindex, "call");
                }

                @Override
                public IModelArchive<IType, IObjectMethodCallsNet> newModelArchive(File location) throws IOException {
                    return new PoolingModelArchive(new CallNetZipModelFactory(location, jdtResolver));
                }
            });
        }
    }
}