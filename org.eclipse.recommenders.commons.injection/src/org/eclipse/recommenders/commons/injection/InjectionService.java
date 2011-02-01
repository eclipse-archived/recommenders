/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.injection;

import java.util.List;

import org.eclipse.recommenders.commons.internal.injection.InjectionDescriptor;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class InjectionService {

    private static InjectionService instance = new InjectionService();

    public static InjectionService getInstance() {
        return instance;
    }

    private Injector lazyInjector;

    public Injector getInjector() {
        if (lazyInjector == null) {
            final List<Module> modules = InjectionDescriptor.createModules();
            lazyInjector = Guice.createInjector(modules);
        }
        return lazyInjector;
    }

    public void injectMembers(final Object obj) {
        getInjector().injectMembers(obj);
    }

    public <T> T requestInstance(final Class<T> clazz) {
        return getInjector().getInstance(clazz);
    }

}
