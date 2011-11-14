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
package org.eclipse.recommenders.injection;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.recommenders.internal.injection.InjectionDescriptor;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public final class InjectionService {

    private static InjectionService instance = new InjectionService(1);

    public static InjectionService getInstance() {
        return instance;
    }

    private InjectionService(final int blockDI) {
        // no need to instantiate this class. Call getInstance instead.
    }

    private Injector lazyInjector;

    ReentrantLock lock = new ReentrantLock();

    public synchronized Injector getInjector() {
        if (lock.isLocked()) {
            throw new IllegalStateException(
                    "reentrant access during injector creation is prohibited! Check your plug-in startup behavior");
        }
        try {
            lock.lock();
            if (lazyInjector == null) {
                final List<Module> modules = InjectionDescriptor.createModules();
                lazyInjector = Guice.createInjector(modules);

            }
        } finally {
            lock.unlock();
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
