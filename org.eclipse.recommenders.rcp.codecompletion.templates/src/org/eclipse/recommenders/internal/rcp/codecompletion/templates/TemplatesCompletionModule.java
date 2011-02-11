/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionEngine;

/**
 * Prepares the <code>Plugin</code> by injecting dependencies.
 */
public final class TemplatesCompletionModule extends AbstractModule {

    @Override
    protected void configure() {
        bindCallsRecommender();
        bindExpressionFormatter();
        bindIntelligentCodecompletionEngine();
    }

    /**
     * Binds the recommender which will return relevant patterns.
     */
    private void bindCallsRecommender() {
        bind(PatternRecommender.class).in(Scopes.SINGLETON);
    }

    /**
     * Binds the formatter which will return a string for a pattern's code.
     */
    private void bindExpressionFormatter() {
        bind(ExpressionFormatter.class).in(Scopes.SINGLETON);
    }

    private void bindIntelligentCodecompletionEngine() {
        final Multibinder<IIntelligentCompletionEngine> binder = Multibinder.newSetBinder(binder(),
                IIntelligentCompletionEngine.class);
        binder.addBinding().to(TemplatesCompletionEngine.class).in(Scopes.SINGLETON);
    }
}
