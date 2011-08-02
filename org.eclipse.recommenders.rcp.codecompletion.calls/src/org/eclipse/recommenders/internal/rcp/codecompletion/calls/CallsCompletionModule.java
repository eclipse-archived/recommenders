/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.internal.rcp.analysis.IRecommendersProjectLifeCycleListener;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.preferences.ClientConfigurationPreferenceListener;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectModelFacadeFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectServices;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.RemoteResolverJobFactory;
import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class CallsCompletionModule extends AbstractModule {

    public static final String CALLS_STORE_LOCATION = "calls.store.location";

    @Override
    protected void configure() {
        configurePreferences();
        configureProjectServices();
        configureArchiveModelStore();
        configureRecommendationsViewPublisher();
    }

    private void configurePreferences() {
        final ClientConfiguration config = new ClientConfiguration();
        final IPreferenceStore store = CallsCompletionPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(new ClientConfigurationPreferenceListener(config, store));
        bind(ClientConfiguration.class).annotatedWith(UdcServer.class).toInstance(config);
    }

    private void configureProjectServices() {
        bind(ProjectServices.class).in(Scopes.SINGLETON);
        final Multibinder<IRecommendersProjectLifeCycleListener> multibinder = Multibinder.newSetBinder(binder(),
                IRecommendersProjectLifeCycleListener.class);
        multibinder.addBinding().to(ProjectServices.class);
    }

    private void configureArchiveModelStore() {
        bind(IModelArchiveStore.class).to(ModelArchiveStore.class).in(Scopes.SINGLETON);

        final IPath stateLocation = Platform.getStateLocation(FrameworkUtil.getBundle(getClass()));
        bind(File.class).annotatedWith(Names.named(CALLS_STORE_LOCATION)).toInstance(
                new File(stateLocation.toFile(), "models"));

        bind(File.class).annotatedWith(ClasspathDependencyStoreLocation.class).toInstance(
                new File(stateLocation.toFile(), "dependencyIndex"));
        bind(ClasspathDependencyStore.class).in(Scopes.SINGLETON);
        install(new FactoryModuleBuilder().build(ProjectModelFacadeFactory.class));
        install(new FactoryModuleBuilder().build(RemoteResolverJobFactory.class));
    }

    private void configureRecommendationsViewPublisher() {
        Multibinder.newSetBinder(binder(), IRecommendationsViewContentProvider.class).addBinding()
                .to(RecommendationsViewPublisherForCalls.class);
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface ClasspathDependencyStoreLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface UdcServer {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface PreferenceStore {
    }
}
