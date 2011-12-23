/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.extdoc.rcp;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import org.eclipse.recommenders.extdoc.rcp.providers.FastProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.SlowAndFailingProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.SlowProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.TooSlowProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.VerySlowProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.javadoc.JavadocProvider;
import org.eclipse.recommenders.extdoc.rcp.ui.ExtdocIconLoader;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

public class ExtdocModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ExtdocIconLoader.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    @Extdoc
    EventBus provideEventBus() {
        return new EventBus("extdoc-eventbus");
    }

    @Provides
    @Singleton
    List<Provider> provideProviders(final ExtdocIconLoader iconLoader, final/* workspace bus */EventBus workspaceBus) {
        // IConfigurationElement[] elements =
        // getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);
        //
        // Set<Class<? extends IProvider>> providers = newHashSet();
        // for (final IConfigurationElement element : elements) {
        // // try {
        // String className = element.getAttribute("class");
        // try {
        // Class<? extends IProvider> forName = (Class<? extends IProvider>)
        // Class.forName(className);
        // providers.add(forName);
        // } catch (ClassNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        final List<Provider> providers = Lists.newArrayList();
        providers.add(new FastProvider(iconLoader));
        providers.add(new SlowProvider(iconLoader));
        providers.add(new SlowAndFailingProvider(iconLoader));
        providers.add(new VerySlowProvider(iconLoader));
        providers.add(new TooSlowProvider(iconLoader));
        providers.add(new JavadocProvider(iconLoader, workspaceBus));

        return providers;
    }

    @BindingAnnotation
    @Target({ METHOD, PARAMETER })
    @Retention(RUNTIME)
    public static @interface Extdoc {
    }
}