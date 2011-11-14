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
package org.eclipse.recommenders.mining.calls.data.zip;

import org.eclipse.recommenders.mining.calls.AbstractBindingModule;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.IModelSpecificationProvider;
import org.eclipse.recommenders.mining.calls.data.IObjectUsageProvider;
import org.eclipse.recommenders.mining.calls.generation.IModelGenerator;
import org.eclipse.recommenders.mining.calls.generation.ModelGenerationListenerLogger;
import org.eclipse.recommenders.mining.calls.generation.callgroups.CallgroupModelGenerator;

public class ZipGuiceModule extends AbstractBindingModule {

	public ZipGuiceModule(final AlgorithmParameters arguments) {
		super(arguments);
	}

	@Override
	protected void specificConfigure() {
		bind(IModelSpecificationProvider.class).to(ZipModelSpecificationProvider.class);
		bind(IObjectUsageProvider.class).to(ZipObjectUsageProvider.class);

		addModelGenerationListener(ModelGenerationListenerLogger.class);
	}

	@Override
	protected void bindModelGenerator() {
		bind(IModelGenerator.class).to(CallgroupModelGenerator.class);
	}
}