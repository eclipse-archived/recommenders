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
package org.eclipse.recommenders.internal.commons.analysis.analyzers.modules;

import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.JavaLangInstanceKeysRemoverCompilationUnitFinalizer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class JavaLangInstanceKeysCompilationUnitFinalizerPluginModule extends AbstractModule {
    @Override
    public void configure() {
        final Multibinder<ICompilationUnitFinalizer> binder = Multibinder.newSetBinder(binder(),
                ICompilationUnitFinalizer.class);
        binder.addBinding().to(JavaLangInstanceKeysRemoverCompilationUnitFinalizer.class).in(Singleton.class);
    }
}