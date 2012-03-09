/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.repo.wiring;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepository;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepositoryIndex;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.rcp.repo.ModelRepositoryService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;

public class GuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bindRepository();
    }

    private void bindRepository() {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File stateLocation = Platform.getStateLocation(bundle).toFile();

        File repo = new File(stateLocation, "repo");
        repo.mkdirs();
        bind(File.class).annotatedWith(LocalModelRepositoryLocation.class).toInstance(repo);
        bind(String.class).annotatedWith(RemoteModelRepositoryLocation.class).toInstance(
                "http://vandyk.st.informatik.tu-darmstadt.de/maven/");
        bind(IModelRepository.class).to(ModelRepository.class).in(Scopes.SINGLETON);

        File index = new File(stateLocation, "index");
        index.mkdirs();
        bind(File.class).annotatedWith(ModelRepositoryIndexLocation.class).toInstance(index);
        bind(IModelRepositoryIndex.class).to(ModelRepositoryIndex.class).in(Scopes.SINGLETON);
        bind(ModelRepositoryService.class).asEagerSingleton();
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface LocalModelRepositoryLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface RemoteModelRepositoryLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface ModelRepositoryIndexLocation {
    }

}
