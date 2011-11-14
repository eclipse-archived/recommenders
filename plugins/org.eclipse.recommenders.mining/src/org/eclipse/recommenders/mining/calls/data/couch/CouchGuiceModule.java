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
package org.eclipse.recommenders.mining.calls.data.couch;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.mining.calls.AbstractBindingModule;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.IModelSpecificationProvider;
import org.eclipse.recommenders.mining.calls.data.IObjectUsageProvider;
import org.eclipse.recommenders.mining.calls.generation.IModelGenerator;
import org.eclipse.recommenders.mining.calls.generation.ModelGenerationListenerLogger;
import org.eclipse.recommenders.mining.calls.generation.callgroups.CallgroupModelGenerator2;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class CouchGuiceModule extends AbstractBindingModule {

	public CouchGuiceModule(AlgorithmParameters args) {
		super(args);
	}

	@Override
	protected void specificConfigure() {
		bind(IModelSpecificationProvider.class).to(CouchModelSpecificationProvider.class);
		bind(IObjectUsageProvider.class).to(CouchObjectUsageProvider.class);

		addModelGenerationListener(ModelGenerationListenerLogger.class);
		addModelGenerationListener(CouchModelSpecUpdater.class);
	}

	@Override
	public void bindModelGenerator() {
		bind(IModelGenerator.class).to(CallgroupModelGenerator2.class);
	}

	@Provides
	@Singleton
	protected WebServiceClient provideWebserviceClient(final AlgorithmParameters args) {
		final String url = args.getHost().toExternalForm();
		final ClientConfiguration config = ClientConfiguration.create(url);
		return new WebServiceClient(config);
	}
}