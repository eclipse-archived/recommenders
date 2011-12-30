/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.mining.calls;

import org.eclipse.recommenders.mining.calls.generation.IModelGenerationListener;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public abstract class AbstractBindingModule extends AbstractModule {

	private final AlgorithmParameters arguments;
	private Multibinder<IModelGenerationListener> listeners;

	public AbstractBindingModule(final AlgorithmParameters arguments) {
		this.arguments = arguments;
	}

	@Override
	protected void configure() {
		bind(AlgorithmParameters.class).toInstance(arguments);

		listeners = Multibinder.newSetBinder(binder(), IModelGenerationListener.class);
		specificConfigure();
		bindModelGenerator();
	}

	protected void addModelGenerationListener(Class<? extends IModelGenerationListener> listenerClass) {
		listeners.addBinding().to(listenerClass);
	}

	protected abstract void specificConfigure();

	protected abstract void bindModelGenerator();
}