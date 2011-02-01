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
package org.eclipse.recommenders.examples.rcp.codecompletion;

import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

public class DemoModule extends AbstractModule {
    @Override
    protected void configure() {
        final Multibinder<IIntelligentCompletionEngine> binder = Multibinder.newSetBinder(binder(),
                IIntelligentCompletionEngine.class);
        binder.addBinding().to(DemoCompletionEngine.class).in(Scopes.SINGLETON);
    }
}
