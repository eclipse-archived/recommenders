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
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.MavenCentralFingerprintSearchAdvisor;
import org.eclipse.recommenders.models.advisors.FingerprintAdvisor;
import org.eclipse.recommenders.models.advisors.JREExecutionEnvironmentAdvisor;
import org.eclipse.recommenders.models.advisors.JREReleaseFileAdvisor;
import org.eclipse.recommenders.models.advisors.MavenPomPropertiesAdvisor;
import org.eclipse.recommenders.models.advisors.MavenPomXmlAdvisor;
import org.eclipse.recommenders.models.advisors.OsgiManifestAdvisor;
import org.eclipse.recommenders.models.advisors.ProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Names;

@SuppressWarnings("restriction")
public class ModelsRcpModule extends AbstractModule implements Module {

    public static final String IDENTIFIED_PACKAGE_FRAGMENT_ROOTS = "IDENTIFIED_PACKAGE_FRAGMENT_ROOTS";
    public static final String REPOSITORY_BASEDIR = "REPOSITORY_BASEDIR";
    public static final String INDEX_BASEDIR = "INDEX_BASEDIR";
    public static final String MANUAL_MAPPINGS = "MANUAL_MAPPINGS";

    @Override
    protected void configure() {
        requestStaticInjection(Dependencies.class);
        //
        bind(IProjectCoordinateProvider.class).to(ProjectCoordinateProvider.class).in(SINGLETON);

        // bind all clients of IRecommendersModelIndex or its super interface IModelArchiveCoordinateProvider to a
        // single instance in Eclipse:
        bind(EclipseModelIndex.class).in(SINGLETON);
        bind(IModelArchiveCoordinateAdvisor.class).to(EclipseModelIndex.class);
        bind(IModelIndex.class).to(EclipseModelIndex.class);
        createAndBindNamedFile("index", INDEX_BASEDIR);

        //
        bind(IModelRepository.class).to(EclipseModelRepository.class).in(SINGLETON);
        createAndBindNamedFile("repository", REPOSITORY_BASEDIR);

        // configure caching
        bind(ManualProjectCoordinateAdvisor.class).in(SINGLETON);
        createAndBindNamedFile("caches/manual-mappings.json", MANUAL_MAPPINGS);
        createAndBindNamedFile("caches/identified-package-fragment-roots.json", IDENTIFIED_PACKAGE_FRAGMENT_ROOTS);
    }

    private void createAndBindNamedFile(String fileName, String name) {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File stateLocation = Platform.getStateLocation(bundle).toFile();
        File file = new File(stateLocation, fileName);
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            e.printStackTrace();
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

    @Singleton
    @Provides
    public ProjectCoordinateAdvisorService provideMappingProvider(IModelIndex index,
            ManualProjectCoordinateAdvisor manualMappingStrategy) {
        ProjectCoordinateAdvisorService mappingProvider = new ProjectCoordinateAdvisorService();
        mappingProvider.addAdvisor(manualMappingStrategy);
        mappingProvider.addAdvisor(new MavenPomPropertiesAdvisor());
        mappingProvider.addAdvisor(new JREExecutionEnvironmentAdvisor());
        mappingProvider.addAdvisor(new JREReleaseFileAdvisor());
        mappingProvider.addAdvisor(new OsgiManifestAdvisor());
        mappingProvider.addAdvisor(new MavenPomXmlAdvisor());
        mappingProvider.addAdvisor(new FingerprintAdvisor(index));
        mappingProvider.addAdvisor(new MavenCentralFingerprintSearchAdvisor());
        return mappingProvider;
    }

    @Provides
    @Singleton
    public ModelsRcpPreferences provide(IWorkbench wb) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        ModelsRcpPreferences prefs = ContextInjectionFactory.make(ModelsRcpPreferences.class, context);
        return prefs;
    }
}
