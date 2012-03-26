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
package org.eclipse.recommenders.internal.extdoc.rcp.wiring;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.ClassSelfcallDirectives;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.archive.CachingModelArchive;
import org.eclipse.recommenders.internal.rcp.models.archive.MemberGsonZipPoolableModelFactory;
import org.eclipse.recommenders.internal.rcp.models.store.DefaultModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.store.IDependenciesFactory;
import org.eclipse.recommenders.internal.rcp.models.store.ModelArchiveResolutionJob;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ManualModelStoreWiring {

    private static String VERSION = "v0.5-";

    static File store(String filename) {
        Bundle bundle = FrameworkUtil.getBundle(ManualModelStoreWiring.class);
        File basedir = Platform.getStateLocation(bundle).toFile();
        return new File(basedir, VERSION + filename);
    }

    public static class ClassOverridesPatternsModelStore extends DefaultModelArchiveStore<IType, ClassOverridePatterns> {

        static final Class gson = ClassOverridePatterns.class;
        static final String classifier = "ovrp";

        @Inject
        public ClassOverridesPatternsModelStore(final IModelRepository repository,
                final JavaElementResolver jdtResolver, final IClasspathEntryInfoProvider cpeInfoProvider,
                final IModelRepositoryIndex searchindex) {
            super(store("class-overrides-patterns.json"), classifier, repository, new IDependenciesFactory() {

                @Override
                public ModelArchiveResolutionJob newResolutionJob(ModelArchiveMetadata metadata, String classifier) {
                    return new ModelArchiveResolutionJob(metadata, cpeInfoProvider, repository, searchindex, classifier);
                }

                @Override
                public IModelArchive newModelArchive(File file) throws IOException {
                    MemberGsonZipPoolableModelFactory loader = new MemberGsonZipPoolableModelFactory(file, gson,
                            jdtResolver);
                    return new CachingModelArchive(loader);
                }
            });
        }
    }

    public static class ClassOverridesModelStore extends DefaultModelArchiveStore<IType, ClassOverrideDirectives> {

        static final Class gson = ClassOverrideDirectives.class;
        static final String classifier = "ovrd";

        @Inject
        public ClassOverridesModelStore(final IModelRepository repository, final JavaElementResolver jdtResolver,
                final IClasspathEntryInfoProvider cpeInfoProvider, final IModelRepositoryIndex searchindex) {
            super(store("class-overrides.json"), classifier, repository, new IDependenciesFactory() {

                @Override
                public ModelArchiveResolutionJob newResolutionJob(ModelArchiveMetadata metadata, String classifier) {
                    return new ModelArchiveResolutionJob(metadata, cpeInfoProvider, repository, searchindex, classifier);
                }

                @Override
                public IModelArchive newModelArchive(File file) throws IOException {
                    MemberGsonZipPoolableModelFactory loader = new MemberGsonZipPoolableModelFactory(file, gson,
                            jdtResolver);
                    return new CachingModelArchive(loader);
                }
            });
        }
    }

    public static class ClassSelfcallsModelStore extends DefaultModelArchiveStore<IType, ClassSelfcallDirectives> {

        static final Class gson = ClassSelfcallDirectives.class;
        static final String classifier = "selfc";

        // XXX static final String classifier = "selfm";

        @Inject
        public ClassSelfcallsModelStore(final IModelRepository repository, final JavaElementResolver jdtResolver,
                final IClasspathEntryInfoProvider cpeInfoProvider, final IModelRepositoryIndex searchindex) {
            super(store("class-selfcalls.json"), classifier, repository, new IDependenciesFactory() {

                @Override
                public ModelArchiveResolutionJob newResolutionJob(final ModelArchiveMetadata metadata, String classifier) {
                    return new ModelArchiveResolutionJob(metadata, cpeInfoProvider, repository, searchindex, classifier);
                }

                @Override
                public IModelArchive newModelArchive(File file) throws IOException {
                    MemberGsonZipPoolableModelFactory loader = new MemberGsonZipPoolableModelFactory(file, gson,
                            jdtResolver);
                    return new CachingModelArchive(loader);
                }
            });
        }
    }

    public static class MethodSelfcallsModelStore extends DefaultModelArchiveStore<IMethod, MethodSelfcallDirectives> {

        static final Class gson = MethodSelfcallDirectives.class;
        static final String classifier = "selfm";

        @Inject
        public MethodSelfcallsModelStore(final IModelRepository repository, final JavaElementResolver jdtResolver,
                final IClasspathEntryInfoProvider cpeInfoProvider, final IModelRepositoryIndex searchindex) {
            super(store("method-selfcalls.json"), classifier, repository, new IDependenciesFactory() {

                @Override
                public ModelArchiveResolutionJob newResolutionJob(final ModelArchiveMetadata metadata, String classifier) {
                    return new ModelArchiveResolutionJob(metadata, cpeInfoProvider, repository, searchindex, classifier);
                }

                @Override
                public IModelArchive newModelArchive(File file) throws IOException {
                    MemberGsonZipPoolableModelFactory loader = new MemberGsonZipPoolableModelFactory(file, gson,
                            jdtResolver);
                    return new CachingModelArchive(loader);
                }
            });
        }
    }
}
