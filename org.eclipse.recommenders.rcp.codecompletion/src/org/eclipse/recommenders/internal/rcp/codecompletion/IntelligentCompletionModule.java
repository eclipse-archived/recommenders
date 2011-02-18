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
package org.eclipse.recommenders.internal.rcp.codecompletion;

import org.eclipse.recommenders.internal.rcp.codecompletion.resolvers.AnonymousMemberAccessVariableUsageResolver;
import org.eclipse.recommenders.internal.rcp.codecompletion.resolvers.AstBasedVariableUsageResolver;
import org.eclipse.recommenders.internal.rcp.codecompletion.resolvers.StoreBasedVariableUsageResolver;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

public class IntelligentCompletionModule extends AbstractModule {

    @Override
    protected void configure() {
        configureVariableUsageResolvers();
        configureContextResolver();
    }

    private void configureContextResolver() {
        bind(IntelligentCompletionContextResolver.class).in(Scopes.SINGLETON);
    }

    private void configureVariableUsageResolvers() {
        final Multibinder<IVariableUsageResolver> b = Multibinder.newSetBinder(binder(), IVariableUsageResolver.class);
        b.addBinding().to(StoreBasedVariableUsageResolver.class);
        b.addBinding().to(AstBasedVariableUsageResolver.class);
        b.addBinding().to(AnonymousMemberAccessVariableUsageResolver.class);
    }
}
