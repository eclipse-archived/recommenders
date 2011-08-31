/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.calls.zip;

import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.IModelGenerationListener;
import org.eclipse.recommenders.mining.calls.IModelSpecificationProvider;
import org.eclipse.recommenders.mining.calls.IObjectUsageProvider;
import org.eclipse.recommenders.mining.calls.ModelGenerationListenerLogger;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ZipGuiceModule extends AbstractModule {
    private final AlgorithmParameters arguments;

    public ZipGuiceModule(final AlgorithmParameters arguments) {
        this.arguments = arguments;
    }

    @Override
    protected void configure() {
        bind(AlgorithmParameters.class).toInstance(arguments);
        bind(IModelSpecificationProvider.class).to(ZipModelSpecificationProvider.class);
        bind(IObjectUsageProvider.class).to(ZipObjectUsageProvider.class);
        final Multibinder<IModelGenerationListener> listeners = Multibinder.newSetBinder(binder(),
                IModelGenerationListener.class);
        listeners.addBinding().to(ModelGenerationListenerLogger.class);

    }
}