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
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides;

import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.eclipse.recommenders.rcp.IArtifactStoreChangedListener;
import org.eclipse.recommenders.rcp.IEditorChangedListener;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

public class OverridesModule extends AbstractModule {
    @Override
    protected void configure() {
        bindCompletionEngine();
    }

    private void bindCompletionEngine() {
        bind(OverridesModelStore.class).in(Scopes.SINGLETON);
        bind(OverridesCompletionEngine.class).in(Scopes.SINGLETON); //
        bind(InstantOverridesRecommender.class).in(Scopes.SINGLETON); //
        Multibinder.newSetBinder(binder(), IEditorChangedListener.class).addBinding()
                .to(OverridesCompletionEngine.class);
        Multibinder.newSetBinder(binder(), IArtifactStoreChangedListener.class).addBinding()
                .to(OverridesCompletionEngine.class);
        Multibinder.newSetBinder(binder(), IIntelligentCompletionEngine.class).addBinding()
                .to(OverridesCompletionEngine.class);
        Multibinder.newSetBinder(binder(), IRecommendationsViewContentProvider.class).addBinding()
                .to(InstantOverridesRecommender.class);
    }
}
