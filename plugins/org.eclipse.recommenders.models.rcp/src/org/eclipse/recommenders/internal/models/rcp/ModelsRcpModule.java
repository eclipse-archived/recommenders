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
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.internal.models.rcp.l10n.LogMessages;
import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.advisors.ModelIndexBundleSymbolicNameAdvisor;
import org.eclipse.recommenders.models.advisors.SharedManualMappingsAdvisor;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.ui.IWorkbench;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class ModelsRcpModule extends AbstractModule {

    private static final String EXT_ID_MODEL_CLASSIFIER = "org.eclipse.recommenders.models.rcp.models"; //$NON-NLS-1$
    private static final String MODEL_CLASSIFIER_ATTRIBUTE = "classifier"; //$NON-NLS-1$

    private static final String EXT_ID_TRANSFORMERS_CLASSIFIER = "org.eclipse.recommenders.models.rcp.transformers"; //$NON-NLS-1$
    private static final String TRANSFORMER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
    private static final String TRANSFORMER_FILE_EXTENSION_ATTRIBUTE = "fileExtension"; //$NON-NLS-1$

    public static final String MODEL_CLASSIFIER = "MODEL_CLASSIFIER"; //$NON-NLS-1$
    public static final String REPOSITORY_BASEDIR = "REPOSITORY_BASEDIR"; //$NON-NLS-1$
    public static final String INDEX_BASEDIR = "INDEX_BASEDIR"; //$NON-NLS-1$

    @Override
    protected void configure() {
        bind(IProjectCoordinateProvider.class).to(ProjectCoordinateProvider.class).in(SINGLETON);

        // bind all clients of IRecommendersModelIndex or its super interface IModelArchiveCoordinateProvider to a
        // single instance in Eclipse:
        bind(EclipseModelIndex.class).in(SINGLETON);
        bind(IModelArchiveCoordinateAdvisor.class).to(EclipseModelIndex.class);
        bind(IModelIndex.class).to(EclipseModelIndex.class);
        createAndBindPerWorkspaceNamedFile("index", INDEX_BASEDIR); //$NON-NLS-1$

        bind(EclipseModelRepository.class).in(SINGLETON);
        bind(IModelRepository.class).to(EclipseModelRepository.class);
        createAndBindPerUserNamedFile("repository", REPOSITORY_BASEDIR); //$NON-NLS-1$
    }

    private void createAndBindPerUserNamedFile(String fileName, String name) {
        File userHome = SystemUtils.getUserHome();
        File dotEclipse = new File(userHome, ".eclipse"); //$NON-NLS-1$
        File stateLocation = new File(dotEclipse, Constants.BUNDLE_ID);
        createAndBindNamedFile(fileName, name, stateLocation);
    }

    private void createAndBindPerWorkspaceNamedFile(String fileName, String name) {
        File workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
        File dotRecommenders = new File(workspaceRoot, ".recommenders"); //$NON-NLS-1$
        createAndBindNamedFile(fileName, name, dotRecommenders);
    }

    private void createAndBindNamedFile(String fileName, String name, File stateLocation) {
        File file = new File(stateLocation, fileName);
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_BIND_FILE_NAME, fileName, e);
        }
        bind(File.class).annotatedWith(Names.named(name)).toInstance(file);
    }

    @Provides
    public ModelIndexBundleSymbolicNameAdvisor provideModelIndexBundleSymbolicNameAdvisor(IModelIndex index) {
        return new ModelIndexBundleSymbolicNameAdvisor(index);
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
        return ContextInjectionFactory.make(ModelsRcpPreferences.class, context);
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

    @Provides
    @Singleton
    public Map<String, IInputStreamTransformer> provideTransformers() {
        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                EXT_ID_TRANSFORMERS_CLASSIFIER);
        ImmutableMap.Builder<String, IInputStreamTransformer> builder = ImmutableMap.builder();
        for (IConfigurationElement element : elements) {
            try {
                IInputStreamTransformer transformer = (IInputStreamTransformer) element
                        .createExecutableExtension(TRANSFORMER_CLASS_ATTRIBUTE);
                String fileExtension = element.getAttribute(TRANSFORMER_FILE_EXTENSION_ATTRIBUTE);
                builder.put(fileExtension, transformer);
            } catch (CoreException e) {
                Logs.log(LogMessages.ERROR_CREATE_EXECUTABLE_EXTENSION_FAILED,
                        element.getAttribute(TRANSFORMER_CLASS_ATTRIBUTE), e);
            }
        }
        return builder.build();
    }
}
