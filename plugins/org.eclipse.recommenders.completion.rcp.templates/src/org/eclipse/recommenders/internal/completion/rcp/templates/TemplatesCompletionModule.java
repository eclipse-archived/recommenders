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
package org.eclipse.recommenders.internal.completion.rcp.templates;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import org.eclipse.recommenders.internal.completion.rcp.templates.code.CodeBuilder;
import org.eclipse.recommenders.internal.completion.rcp.templates.code.MethodCallFormatter;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

/**
 * Prepares the <code>Plugin</code> by injecting dependencies.
 */
public final class TemplatesCompletionModule extends AbstractModule {

    @Override
    protected void configure() {
        bindCallsRecommender();
        bindCodeBuilder();
    }

    /**
     * Binds the recommender which will return relevant patterns.
     */
    private void bindCallsRecommender() {
        bind(PatternRecommender.class).in(Scopes.SINGLETON);
    }

    /**
     * Binds the {@link CodeBuilder} which will return a string of java code for
     * a pattern's method calls.
     */
    private void bindCodeBuilder() {
        bind(JavaElementResolver.class).in(Scopes.SINGLETON);
        bind(MethodCallFormatter.class).in(Scopes.SINGLETON);
        bind(CodeBuilder.class).in(Scopes.SINGLETON);
    }
}
