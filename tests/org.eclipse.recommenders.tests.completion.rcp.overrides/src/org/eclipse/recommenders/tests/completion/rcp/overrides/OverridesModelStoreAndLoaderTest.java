/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.completion.rcp.overrides;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.recommenders.internal.completion.rcp.overrides.IOverridesModelLoader;
import org.eclipse.recommenders.internal.completion.rcp.overrides.OverridesModelLoader;
import org.eclipse.recommenders.internal.completion.rcp.overrides.OverridesModelStore;
import org.eclipse.recommenders.internal.completion.rcp.overrides.net.ClassOverridesNetwork;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class OverridesModelStoreAndLoaderTest {

    @Test
    public void testLoadModelInZip() {
        // setup:
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IOverridesModelLoader.class).to(OverridesModelLoader.class).in(Scopes.SINGLETON);
                bind(URL.class).annotatedWith(Names.named("overrides.model.fileUrl"))
                        .toInstance(getCallsModelFileUrl());
            }
        });
        final OverridesModelStore store = injector.getInstance(OverridesModelStore.class);

        // exercise:
        final VmTypeName someName = VmTypeName.get("Lorg/eclipse/compare/BufferedContent");
        final ClassOverridesNetwork model = store.getModel(someName);
        final boolean hasBufferedContent = store.hasModel(someName);

        final VmTypeName anotherName = VmTypeName.get("Lorg/eclipse/compare/CompareConfiguration");
        final boolean hasCompareConfiguration = store.hasModel(anotherName);

        // verify:
        Assert.assertTrue(hasBufferedContent);
        Assert.assertFalse(hasCompareConfiguration);
        Assert.assertNotNull(model);
    }

    private URL getCallsModelFileUrl() {
        final Path basedir = new Path("/resources/models.zip.test");
        try {
            final Bundle bundle = FrameworkUtil.getBundle(getClass());
            final URL find = FileLocator.find(bundle, basedir, null);
            return FileLocator.resolve(find);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
