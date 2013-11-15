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
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.core.internal.net.ProxyManager;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.advisors.JREDirectoryNameAdvisor;
import org.eclipse.recommenders.models.advisors.JREExecutionEnvironmentAdvisor;
import org.eclipse.recommenders.models.advisors.JREReleaseFileAdvisor;
import org.eclipse.recommenders.models.advisors.MavenCentralFingerprintSearchAdvisor;
import org.eclipse.recommenders.models.advisors.MavenPomPropertiesAdvisor;
import org.eclipse.recommenders.models.advisors.MavenPomXmlAdvisor;
import org.eclipse.recommenders.models.advisors.ModelIndexBundleSymbolicNameAdvisor;
import org.eclipse.recommenders.models.advisors.ModelIndexFingerprintAdvisor;
import org.eclipse.recommenders.models.advisors.OsgiManifestAdvisor;
import org.eclipse.recommenders.models.advisors.ProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Names;

@SuppressWarnings("restriction")
public class ModelsRcpModule extends AbstractModule implements Module {

    private static final Logger LOG = LoggerFactory.getLogger(ModelsRcpModule.class);

    public static final String IDENTIFIED_PACKAGE_FRAGMENT_ROOTS = "IDENTIFIED_PACKAGE_FRAGMENT_ROOTS";
    public static final String REPOSITORY_BASEDIR = "REPOSITORY_BASEDIR";
    public static final String INDEX_BASEDIR = "INDEX_BASEDIR";
    public static final String MANUAL_MAPPINGS = "MANUAL_MAPPINGS";
    public static final String AVAILABLE_ADVISORS = "DEFAULT_ADVISORS";

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
        bind(EclipseModelRepository.class).in(SINGLETON);
        bind(IModelRepository.class).to(EclipseModelRepository.class);
        createAndBindNamedFile("repository", REPOSITORY_BASEDIR);

        // configure caching
        bind(ManualProjectCoordinateAdvisor.class).in(SINGLETON);
        createAndBindNamedFile("caches/manual-mappings.json", MANUAL_MAPPINGS);
        createAndBindNamedFile("caches/identified-project-coordinates.json", IDENTIFIED_PACKAGE_FRAGMENT_ROOTS);
    }

    private void createAndBindNamedFile(String fileName, String name) {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File stateLocation = Platform.getStateLocation(bundle).toFile();
        File file = new File(stateLocation, fileName);
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            LOG.error("failed to bind file name", e);
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
    @Named(AVAILABLE_ADVISORS)
    public List<String> provideAdvisors() {
        List<String> availableAdvisors = Lists.newArrayList();
        ManualProjectCoordinateAdvisor.class.getName();
        availableAdvisors.add(ManualProjectCoordinateAdvisor.class.getName());
        availableAdvisors.add(MavenPomPropertiesAdvisor.class.getName());
        availableAdvisors.add(JREExecutionEnvironmentAdvisor.class.getName());
        availableAdvisors.add(JREReleaseFileAdvisor.class.getName());
        availableAdvisors.add(JREDirectoryNameAdvisor.class.getName());
        availableAdvisors.add(MavenPomXmlAdvisor.class.getName());
        availableAdvisors.add(ModelIndexBundleSymbolicNameAdvisor.class.getName());
        availableAdvisors.add(ModelIndexFingerprintAdvisor.class.getName());
        availableAdvisors.add(OsgiManifestAdvisor.class.getName());
        availableAdvisors.add(MavenCentralFingerprintSearchAdvisor.class.getName());
        availableAdvisors.add("!" + NestedJarProjectCoordinateAdvisor.class.getName());
        return ImmutableList.copyOf(availableAdvisors);
    }

    @Singleton
    @Provides
    public ProjectCoordinateAdvisorService provideMappingProvider(ModelsRcpPreferences prefs, IModelIndex index,
            ManualProjectCoordinateAdvisor manualMappingStrategy) throws Exception {
        List<IProjectCoordinateAdvisor> availableAdvisors = Lists.newArrayList();
        ProjectCoordinateAdvisorService mappingProvider = new ProjectCoordinateAdvisorService();
        for (String advisorName : prefs.advisors.split(";")) {
            IProjectCoordinateAdvisor advisor = createAdvisor(advisorName, manualMappingStrategy, index).orNull();
            if (advisor != null){
                availableAdvisors.add(advisor);
            }
        }
        mappingProvider.setAdvisors(Advisors.createAdvisorList(availableAdvisors, prefs.advisors));
        return mappingProvider;
    }

    private Optional<IProjectCoordinateAdvisor> createAdvisor(String advisorName,
            ManualProjectCoordinateAdvisor manualMappingStrategy, IModelIndex index) {
        if (advisorName.equals(ManualProjectCoordinateAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(manualMappingStrategy);
        }
        if (advisorName.equals(MavenPomPropertiesAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new MavenPomPropertiesAdvisor());
        }
        if (advisorName.equals(JREExecutionEnvironmentAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new MavenPomPropertiesAdvisor());
        }
        if (advisorName.equals(JREReleaseFileAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new JREReleaseFileAdvisor());
        }
        if (advisorName.equals(JREDirectoryNameAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new JREDirectoryNameAdvisor());
        }
        if (advisorName.equals(MavenPomXmlAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new MavenPomXmlAdvisor());
        }
        if (advisorName.equals(ModelIndexBundleSymbolicNameAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new ModelIndexBundleSymbolicNameAdvisor(index));
        }
        if (advisorName.equals(ModelIndexFingerprintAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new ModelIndexFingerprintAdvisor(index));
        }
        if (advisorName.equals(OsgiManifestAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new OsgiManifestAdvisor());
        }
        if (advisorName.equals(MavenCentralFingerprintSearchAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new MavenCentralFingerprintSearchAdvisor());
        }
        if (advisorName.equals(NestedJarProjectCoordinateAdvisor.class.getName())) {
            return Optional.<IProjectCoordinateAdvisor>of(new NestedJarProjectCoordinateAdvisor());
        }
        return Optional.<IProjectCoordinateAdvisor>absent();
    }

    @Provides
    @Singleton
    public ModelsRcpPreferences provide(IWorkbench wb) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        ModelsRcpPreferences prefs = ContextInjectionFactory.make(ModelsRcpPreferences.class, context);
        return prefs;
    }
}
