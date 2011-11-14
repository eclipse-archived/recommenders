/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers;

import org.eclipse.recommenders.completion.rcp.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.internal.completion.rcp.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.ElementResolver;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public final class ExtDocProvidersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IntelligentCompletionContextResolver.class).in(Scopes.SINGLETON);
        bind(TemplatesCompletionProposalComputer.class).in(Scopes.SINGLETON);

        requestStaticInjection(ElementResolver.class);
    }
}
