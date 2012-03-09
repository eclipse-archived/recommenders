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

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.ClientConfigurationPreferenceListener;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.SectionsFactory;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelDownloadJob.JobFactory;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CallsCompletionModule extends AbstractModule {

    public static final String MODEL_VERSION = "0.4";
    public static final String CALLS_STORE_LOCATION = "calls.store.location";

    @Override
    protected void configure() {
        configurePreferences();
        configureStore3();
    }

    private void configureStore3() {
        final IPath stateLocation = Platform.getStateLocation(FrameworkUtil.getBundle(getClass()));
        final File index = new File(stateLocation.toFile(), MODEL_VERSION + "/index.json");
        bind(File.class).annotatedWith(CallModelsIndexFile.class).toInstance(index);
        install(new FactoryModuleBuilder().build(JobFactory.class));
        install(new FactoryModuleBuilder().build(SectionsFactory.class));
        bind(CallModelStore.class).in(Scopes.SINGLETON);
    }

    private void configurePreferences() {
        final ClientConfiguration config = new ClientConfiguration();
        final IPreferenceStore store = CallsCompletionPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(new ClientConfigurationPreferenceListener(config, store));
        bind(ClientConfiguration.class).annotatedWith(CallModelsServer.class).toInstance(config);
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface DependencyStoreLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface CallModelsIndexFile {
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

}
