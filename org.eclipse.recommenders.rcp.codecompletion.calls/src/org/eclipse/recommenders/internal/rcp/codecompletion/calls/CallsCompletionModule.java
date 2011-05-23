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

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayesnet.NewBNCallsModelStore;
import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class CallsCompletionModule extends AbstractModule {
    @Override
    protected void configure() {
        configureModelStore();
        configureRecommendationsViewPublisher();
    }

    private void configureModelStore() {
        // configureJsonModelStore();
        configureBinaryModelStore();
    }

    private void configureJsonModelStore() {
        bind(ICallsModelLoader.class).to(CallsModelLoader.class).in(Scopes.SINGLETON);
        bind(CallsModelStore.class).in(Scopes.SINGLETON);
        final Path basedir = new Path("/data/models.zip");
        final URL getModelFileUrl = getCallsModelFileUrl(basedir);
        bind(URL.class).annotatedWith(Names.named("calls.model.fileUrl")).toInstance(getModelFileUrl);

    }

    private void configureBinaryModelStore() {
        bind(ICallsModelLoader.class).to(BinaryCallsModelLoader.class).in(Scopes.SINGLETON);
        bind(CallsModelStore.class).to(NewBNCallsModelStore.class).in(Scopes.SINGLETON);
        final Path modelFile = new Path("/data/models.zip");
        final URL modelFileUrl = getCallsModelFileUrl(modelFile);
        bind(URL.class).annotatedWith(Names.named("calls.model.fileUrl")).toInstance(modelFileUrl);

    }

    private void configureRecommendationsViewPublisher() {
        Multibinder.newSetBinder(binder(), IRecommendationsViewContentProvider.class).addBinding()
                .to(RecommendationsViewPublisherForCalls.class);
    }

    private URL getCallsModelFileUrl(final Path path) {
        try {
            return FileLocator.resolve(FileLocator.find(FrameworkUtil.getBundle(getClass()), path, null));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
