/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.inject.Scopes.SINGLETON;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import org.eclipse.core.internal.net.ProxyManager;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.advisors.ModelIndexBundleSymbolicNameAdvisor;
import org.eclipse.recommenders.models.advisors.ModelIndexFingerprintAdvisor;
import org.eclipse.recommenders.models.advisors.SharedManualMappingsAdvisor;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

@SuppressWarnings("restriction")
public class ModelsRcpModule extends AbstractModule implements Module {

    private static final String EXT_ID_MODEL_CLASSIFIER = "org.eclipse.recommenders.models.rcp.models"; //$NON-NLS-1$
    private static final String MODEL_CLASSIFIER_ATTRIBUTE = "classifier"; //$NON-NLS-1$

    public static final String IDENTIFIED_PROJECT_COORDINATES = "IDENTIFIED_PACKAGE_FRAGMENT_ROOTS"; //$NON-NLS-1$
    public static final String MODEL_CLASSIFIER = "MODEL_CLASSIFIER"; //$NON-NLS-1$
    public static final String REPOSITORY_BASEDIR = "REPOSITORY_BASEDIR"; //$NON-NLS-1$
    public static final String INDEX_BASEDIR = "INDEX_BASEDIR"; //$NON-NLS-1$
    public static final String MANUAL_MAPPINGS = "MANUAL_MAPPINGS"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(ModelsRcpModule.class);

    @Override
    protected void configure() {
        requestStaticInjection(Dependencies.class);
        //
        bind(IProjectCoordinateProvider.class).to(ProjectCoordinateProvider.class).in(SINGLETON);

        bind(EclipseProjectCoordinateAdvisorService.class).in(SINGLETON);
        bind(IProjectCoordinateAdvisorService.class).to(EclipseProjectCoordinateAdvisorService.class);

        // bind all clients of IRecommendersModelIndex or its super interface IModelArchiveCoordinateProvider to a
        // single instance in Eclipse:
        bind(EclipseModelIndex.class).in(SINGLETON);
        bind(IModelArchiveCoordinateAdvisor.class).to(EclipseModelIndex.class);
        bind(IModelIndex.class).to(EclipseModelIndex.class);
        createAndBindNamedFile("index", INDEX_BASEDIR); //$NON-NLS-1$

        //
        bind(EclipseModelRepository.class).in(SINGLETON);
        bind(IModelRepository.class).to(EclipseModelRepository.class);
        createAndBindNamedFile("repository", REPOSITORY_BASEDIR); //$NON-NLS-1$

        // configure caching
        bind(ManualProjectCoordinateAdvisor.class).in(SINGLETON);
        createAndBindNamedFile("caches/manual-mappings.json", MANUAL_MAPPINGS); //$NON-NLS-1$
        createAndBindNamedFile("caches/identified-project-coordinates.json", IDENTIFIED_PROJECT_COORDINATES); //$NON-NLS-1$

    }

    private void createAndBindNamedFile(String fileName, String name) {
        File rootLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
        File stateLocation = new File(rootLocation, ".recommenders"); //$NON-NLS-1$
        File file = new File(stateLocation, fileName);
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            LOG.error("Failed to bind file name {}.", fileName, e); //$NON-NLS-1$
        }
        bind(File.class).annotatedWith(Names.named(name)).toInstance(file);
    }

    @Singleton
    @Provides
    public EclipseDependencyListener provideMappingProvider(EventBus bus) {
        return new EclipseDependencyListener(bus);
    }

    @Provides
    public IProxyService provideProxyService() {
        return ProxyManager.getProxyManager();
    }

    @Provides
    public ModelIndexBundleSymbolicNameAdvisor provideModelIndexBundleSymbolicNameAdvisor(IModelIndex index) {
        return new ModelIndexBundleSymbolicNameAdvisor(index);
    }

    @Provides
    public ModelIndexFingerprintAdvisor provideModelIndexFingerprintAdvisor(IModelIndex index) {
        return new ModelIndexFingerprintAdvisor(index);
    }

    @Provides
    public SharedManualMappingsAdvisor provideWorkspaceMappingsAdvisor(IModelRepository repository) {
        return new SharedManualMappingsAdvisor(repository);
    }

    @Provides
    @Singleton
    public ModelsRcpPreferences provide(IWorkbench wb, EventBus bus) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        context.set(EventBus.class, bus);
        ModelsRcpPreferences prefs = ContextInjectionFactory.make(ModelsRcpPreferences.class, context);
        return prefs;
    }

    @Provides
    @Named(MODEL_CLASSIFIER)
    public ImmutableSet<String> provideModelClassifiers() {

        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                EXT_ID_MODEL_CLASSIFIER);

        Builder<String> builder = ImmutableSet.builder();
        for (IConfigurationElement element : elements) {
            String classifier = element.getAttribute(MODEL_CLASSIFIER_ATTRIBUTE);
            builder.add(classifier);
        }

        return builder.build();
    }
}
