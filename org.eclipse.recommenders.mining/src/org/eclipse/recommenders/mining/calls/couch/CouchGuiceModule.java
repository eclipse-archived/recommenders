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
package org.eclipse.recommenders.mining.calls.couch;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.IModelGenerationListener;
import org.eclipse.recommenders.mining.calls.IModelSpecificationProvider;
import org.eclipse.recommenders.mining.calls.IObjectUsageProvider;
import org.eclipse.recommenders.mining.calls.ModelGenerationListenerLogger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class CouchGuiceModule extends AbstractModule {
    private final AlgorithmParameters arguments;

    public CouchGuiceModule(final AlgorithmParameters arguments) {
        this.arguments = arguments;
    }

    @Override
    protected void configure() {
        bind(AlgorithmParameters.class).toInstance(arguments);
        bind(IModelSpecificationProvider.class).to(CouchModelSpecificationProvider.class);
        bind(IObjectUsageProvider.class).to(CouchObjectUsageProvider.class);
        final Multibinder<IModelGenerationListener> listeners = Multibinder.newSetBinder(binder(),
                IModelGenerationListener.class);
        listeners.addBinding().to(ModelGenerationListenerLogger.class);
        listeners.addBinding().to(CouchModelSpecUpdater.class);
    }

    @Provides
    @Singleton
    protected WebServiceClient provideWebserviceClient(final AlgorithmParameters args) {
        final String url = args.getHost().toExternalForm();
        final ClientConfiguration config = ClientConfiguration.create(url);
        return new WebServiceClient(config);
    }
}