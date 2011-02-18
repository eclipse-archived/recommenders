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
package org.eclipse.recommenders.tests.rcp.codecompletion.overrides.unit;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.IOverridesModelLoader;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.OverridesModelLoader;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.OverridesModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net.ClassOverridesNetwork;
import org.junit.Assert;
import org.junit.Test;
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
        final ClassOverridesNetwork model = store.getModel(VmTypeName.get("Lorg/eclipse/compare/BufferedContent"));
        final boolean hasBufferedContent = store.hasModel(VmTypeName.get("Lorg/eclipse/compare/BufferedContent"));
        final boolean hasCompareConfiguration = store.hasModel(VmTypeName
                .get("Lorg/eclipse/compare/CompareConfiguration"));

        // verify:
        Assert.assertTrue(hasBufferedContent);
        Assert.assertFalse(hasCompareConfiguration);
        Assert.assertNotNull(model);
    }

    private URL getCallsModelFileUrl() {
        final Path basedir = new Path("/test-data/models.zip.test");
        try {
            return FileLocator.resolve(FileLocator.find(FrameworkUtil.getBundle(getClass()), basedir, null));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
