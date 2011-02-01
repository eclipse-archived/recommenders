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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

public class CallsCompletionModule extends AbstractModule {
    @Override
    protected void configure() {
        configureModelStore();
        configureCompletionEngine();
        configureRecommendationsViewPublisher();
    }

    private void configureModelStore() {
        bind(CallsModelStore.class).in(Scopes.SINGLETON);

    }

    private void configureCompletionEngine() {
        final Multibinder<IIntelligentCompletionEngine> binder = Multibinder.newSetBinder(binder(),
                IIntelligentCompletionEngine.class);
        binder.addBinding().to(CallsCompletionEngine.class).in(Scopes.SINGLETON);
    }

    private void configureRecommendationsViewPublisher() {
        Multibinder.newSetBinder(binder(), IRecommendationsViewContentProvider.class).addBinding()
                .to(RecommendationsViewPublisherForCalls.class);
    }
}
