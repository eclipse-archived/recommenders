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

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.db.CallsModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class CallsCompletionModule extends AbstractModule {
    @Override
    protected void configure() {
        configureArchiveModelStore();
        configureRecommendationsViewPublisher();
    }

    private void configureArchiveModelStore() {
        bind(ICallsModelStore.class).to(CallsModelArchiveStore.class).in(Scopes.SINGLETON);

        final IPath stateLocation = Platform.getStateLocation(FrameworkUtil.getBundle(getClass()));
        bind(File.class).annotatedWith(Names.named("calls.store.location")).toInstance(stateLocation.toFile());
    }

    private void configureRecommendationsViewPublisher() {
        Multibinder.newSetBinder(binder(), IRecommendationsViewContentProvider.class).addBinding()
                .to(RecommendationsViewPublisherForCalls.class);
    }
}
