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
package org.eclipse.recommenders.internal.completion.rcp.overrides.wiring;

import org.eclipse.recommenders.internal.completion.rcp.overrides.OverridesCompletionProposalComputer;
import org.eclipse.recommenders.internal.completion.rcp.overrides.model.InstantOverridesRecommender;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class OverridesModule extends AbstractModule {
    @Override
    protected void configure() {
        bindCompletionEngine();
    }

    private void bindCompletionEngine() {
        bind(OverridesCompletionProposalComputer.class).in(Scopes.SINGLETON);
        bind(InstantOverridesRecommender.class).in(Scopes.SINGLETON);
    }
}
