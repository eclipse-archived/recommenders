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

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Singleton;

import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.models.FingerprintStrategy;
import org.eclipse.recommenders.models.IMappingProvider;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.JREExecutionEnvironmentStrategy;
import org.eclipse.recommenders.models.JREReleaseFileStrategy;
import org.eclipse.recommenders.models.MappingProvider;
import org.eclipse.recommenders.models.MavenPomPropertiesStrategy;
import org.eclipse.recommenders.models.OsgiManifestStrategy;
import org.eclipse.recommenders.models.SimpleIndexSearcher;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;

public class ModelsRcpModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        //
        bind(IProjectCoordinateProvider.class).to(ProjectCoordinateProvider.class).in(Scopes.SINGLETON);
        bind(IModelRepository.class).to(EclipseModelRepository.class).in(Scopes.SINGLETON);
        bindRepository();
    }

    private void bindRepository() {

        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File stateLocation = Platform.getStateLocation(bundle).toFile();

        File repo = new File(stateLocation, "repository"); //$NON-NLS-1$
        repo.mkdirs();
        bind(File.class).annotatedWith(LocalModelRepositoryLocation.class).toInstance(repo);

        File index = new File(stateLocation, "index"); //$NON-NLS-1$
        index.mkdirs();
        bind(File.class).annotatedWith(ModelRepositoryIndexLocation.class).toInstance(index);
    }

    @Singleton
    @Provides
    protected EclipseDependencyListener provideMappingProvider(EventBus bus) {
        return new EclipseDependencyListener(bus);
    }

    @Singleton
    @Provides
    public SimpleIndexSearcher provideSearcher(@ModelRepositoryIndexLocation File localRepositoryFile) {
        return new SimpleIndexSearcher(localRepositoryFile);
    }

    @Singleton
    @Provides
    protected IMappingProvider provideMappingProvider(SimpleIndexSearcher searcher) {
        MappingProvider mappingProvider = new MappingProvider();
        mappingProvider.addStrategy(new MavenPomPropertiesStrategy());
        mappingProvider.addStrategy(new JREExecutionEnvironmentStrategy());
        mappingProvider.addStrategy(new JREReleaseFileStrategy());
        mappingProvider.addStrategy(new OsgiManifestStrategy());
        mappingProvider.addStrategy(new FingerprintStrategy(searcher));
        return mappingProvider;
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface LocalModelRepositoryLocation {
    }

    //
    // @BindingAnnotation
    // @Target(PARAMETER)
    // @Retention(RUNTIME)
    // public static @interface RemoteModelRepositoryLocation {
    // }
    //
    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface ModelRepositoryIndexLocation {
    }

}
