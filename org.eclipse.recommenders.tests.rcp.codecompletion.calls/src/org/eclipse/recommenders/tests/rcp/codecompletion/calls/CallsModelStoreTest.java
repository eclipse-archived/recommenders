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
package org.eclipse.recommenders.tests.rcp.codecompletion.calls;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.ICallsModelLoader;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.InstanceUsage;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.ObjectMethodCallsNet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CallsModelStoreTest {

    private MockCallsModelLoader loader;
    private CallsModelStore store;

    @Before
    public void setup() {
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                loader = new MockCallsModelLoader();
                bind(ICallsModelLoader.class).toInstance(loader);
            }
        });
        store = injector.getInstance(CallsModelStore.class);
    }

    @Test
    public void testSupportedTypes() {
        loader.availableTypes.add(VmTypeName.BOOLEAN);
        loader.availableTypes.add(VmTypeName.BYTE);
        loader.availableTypes.add(VmTypeName.CHAR);
        loader.availableTypes.add(VmTypeName.DOUBLE);

        Assert.assertTrue(store.hasModel(VmTypeName.BOOLEAN));
        Assert.assertTrue(store.hasModel(VmTypeName.BYTE));
        Assert.assertTrue(store.hasModel(VmTypeName.CHAR));
        Assert.assertTrue(store.hasModel(VmTypeName.DOUBLE));
        Assert.assertFalse(store.hasModel(VmTypeName.FLOAT));
        Assert.assertFalse(store.hasModel(VmTypeName.INT));
    }

    @Test
    public void smokeTestModelRetrieval() {
        // setup
        loader.availableTypes.add(VmTypeName.BOOLEAN);
        final LinkedList<InstanceUsage> observations = new LinkedList<InstanceUsage>();
        final InstanceUsage usage = new InstanceUsage();
        usage.name = "InstanceName";
        usage.invokedMethods.add(VmMethodName.get("Ljava/lang/Boolean.toString()Ljava/lang/String;"));
        usage.observedContexts.put(VmMethodName.get("Ljava/lang/Boolean.toString()Ljava/lang/String;"), 1);
        usage.observedContexts.put(VmMethodName.get("Ljava/lang/String.toString()Ljava/lang/String;"), 1);
        observations.add(usage);
        loader.observations.put(VmTypeName.BOOLEAN, observations);

        // exercise
        final ObjectMethodCallsNet model = store.getModel(VmTypeName.BOOLEAN);

        // verify
        Assert.assertNotNull(model);
    }

    private class MockCallsModelLoader implements ICallsModelLoader {

        private final Set<ITypeName> availableTypes = new HashSet<ITypeName>();
        private final Map<ITypeName, List<InstanceUsage>> observations = new HashMap<ITypeName, List<InstanceUsage>>();

        @Override
        public Set<ITypeName> readAvailableTypes() {
            return availableTypes;
        }

        @Override
        public <T> T loadObjectForTypeName(final ITypeName name, final Type returnType) throws IOException {
            return (T) observations.get(name);
        }

    }
}
