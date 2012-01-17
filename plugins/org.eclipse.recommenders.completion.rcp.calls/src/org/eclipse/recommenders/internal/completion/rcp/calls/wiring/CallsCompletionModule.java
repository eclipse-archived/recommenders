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
package org.eclipse.recommenders.internal.completion.rcp.calls.wiring;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.ClientConfigurationPreferenceListener;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallsModelLoader;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.DependencyInfoComputerService;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.DependencyInfoStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.ManifestResolverService;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.ModelArchiveDownloadService;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.osgi.framework.FrameworkUtil;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Provides;

public class CallsCompletionModule extends AbstractModule {

    public static final String MODEL_VERSION = "0.4";
    public static final String CALLS_STORE_LOCATION = "calls.store.location";

    @Override
    protected void configure() {
        configurePreferences();
        store2Configure();
    }

    private void store2Configure() {
        bind(ServicesInitializer.class).asEagerSingleton();
    }

    private void configurePreferences() {
        final ClientConfiguration config = new ClientConfiguration();
        final IPreferenceStore store = CallsCompletionPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(new ClientConfigurationPreferenceListener(config, store));
        bind(ClientConfiguration.class).annotatedWith(CallModelsServer.class).toInstance(config);
    }

    @Provides
    @Singleton
    CallModelStore provideCallModelStore(@CallModelsServer final ClientConfiguration config,
            final JavaElementResolver jdtCache, final EventBus wsBus) {
        final IPath stateLocation = Platform.getStateLocation(FrameworkUtil.getBundle(getClass()));
        final File modelStoreLocation = new File(stateLocation.toFile(), MODEL_VERSION + "/model-store2/");
        final File dependencyStoreLocation = new File(stateLocation.toFile(), MODEL_VERSION + "/dependency-store2/");
        //
        final EventBus callBus = new AsyncEventBus(Executors.newFixedThreadPool(5));
        final DependencyInfoStore depStore = new DependencyInfoStore(dependencyStoreLocation, callBus);
        final DependencyInfoComputerService depInfoComputer = new DependencyInfoComputerService(callBus);
        final ManifestResolverService manifestResolver = new ManifestResolverService(config, callBus);
        final org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.ModelArchiveStore<IObjectMethodCallsNet> modelStore = new org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.ModelArchiveStore<IObjectMethodCallsNet>(
                modelStoreLocation, new CallsModelLoader(), callBus);
        final ModelArchiveDownloadService modelDownloader = new ModelArchiveDownloadService(config, callBus);

        wsBus.register(depStore);
        callBus.register(depStore);
        callBus.register(modelStore);
        callBus.register(depInfoComputer);
        callBus.register(manifestResolver);
        callBus.register(modelDownloader);
        return new CallModelStore(depStore, modelStore, jdtCache);
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface DependencyStoreLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface ModelsStoreLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface CallModelsServer {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface PreferenceStore {
    }

    /*
     * this is a bit odd. Used to initialize complex wired elements such as JavaElementsProvider etc.
     */
    public static class ServicesInitializer {

        @Inject
        private ServicesInitializer(final CallModelStore store) {
            ensureIsNotNull(store);
        }
    }

}
