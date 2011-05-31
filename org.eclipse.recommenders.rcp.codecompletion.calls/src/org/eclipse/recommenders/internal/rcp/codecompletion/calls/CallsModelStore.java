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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.recommenders.commons.utils.FixedSizeLinkedHashMap;
import org.eclipse.recommenders.commons.utils.annotations.Clumsy;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.InstanceUsage;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.NetworkBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.ObjectMethodCallsNet;

import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.internal.util.Sets;

@Clumsy
public class CallsModelStore implements ICallsModelStore {

    @Inject
    protected ICallsModelLoader loader;

    private Set<ITypeName> supportedTypes;

    private final Map<ITypeName, IObjectMethodCallsNet> loadedNetworks = FixedSizeLinkedHashMap.create(100);

    private void init() {
        if (supportedTypes == null) {
            supportedTypes = loader.readAvailableTypes();
        }
    }

    @Override
    public boolean hasModel(@Nullable final ITypeName name) {
        init();
        return name == null ? false : supportedTypes.contains(name);
    }

    @Override
    public IObjectMethodCallsNet getModel(final ITypeName name) {
        ensureIsTrue(hasModel(name));
        //
        IObjectMethodCallsNet network = loadedNetworks.get(name);
        if (network == null) {
            network = loadNetwork(name);
            loadedNetworks.put(name, network);
        }
        return network;
    }

    @Override
    public Set<IObjectMethodCallsNet> getModelsForSimpleName(final ITypeName simpleName) {
        Preconditions.checkArgument("".equals(simpleName.getPackage().getIdentifier()));
        init();
        final Set<IObjectMethodCallsNet> models = Sets.newHashSet();
        final String expectedClassName = simpleName.getClassName().substring(1);
        for (final ITypeName supportedType : supportedTypes) {
            if (supportedType.getClassName().equals(expectedClassName)) {
                models.add(getModel(supportedType));
            }
        }
        return models;
    }

    protected IObjectMethodCallsNet loadNetwork(final ITypeName name) {
        final StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        try {
            stopwatch.split();
            final List<InstanceUsage> usages = loadRelevantUsages(name);
            System.out.printf("deserialization of '%s' took %s\n", name, stopwatch);
            stopwatch.unsplit();

            return createNetwork(name, usages);
        } catch (final IOException x) {
            throw throwUnhandledException(x);
        } finally {
            stopwatch.stop();
            System.out.printf("loading model for '%s' took %s\n", name, stopwatch);
        }
    }

    private List<InstanceUsage> loadRelevantUsages(final ITypeName name) throws IOException {
        final Type listType = new TypeToken<List<InstanceUsage>>() {
        }.getType();
        final List<InstanceUsage> usages = loader.loadObjectForTypeName(name, listType);
        for (final Iterator<InstanceUsage> it = usages.iterator(); it.hasNext();) {
            final InstanceUsage next = it.next();
            if (next.invokedMethods.isEmpty()) {
                it.remove();
                break;
            }
        }
        return usages;
    }

    private ObjectMethodCallsNet createNetwork(final ITypeName name, final List<InstanceUsage> usages) {
        final NetworkBuilder b = new NetworkBuilder(name, usages);
        b.createContextNode();
        b.createAvailabilityNode();
        b.createPatternsNode();
        b.createMethodNodes();
        return b.build();
    }
}
