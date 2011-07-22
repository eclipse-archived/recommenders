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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayes.BayesNetWrapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class CallsModelStore {

    @Inject
    protected ICallsModelLoader loader;

    private Set<ITypeName> supportedTypes;

    private final GenericKeyedObjectPool pool = createPool();

    private GenericKeyedObjectPool createPool() {
        final GenericKeyedObjectPool pool = new GenericKeyedObjectPool(new CallsModelPoolFactory());
        pool.setMaxTotal(100);
        pool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL);
        return pool;
    }

    private void readSupportedTypes() {
        if (supportedTypes == null) {
            supportedTypes = loader.readAvailableTypes();
        }
    }

    public boolean hasModel(@Nullable final ITypeName name) {
        readSupportedTypes();
        return name == null ? false : supportedTypes.contains(name);
    }

    public IObjectMethodCallsNet acquireModel(final ITypeName name) {
        ensureIsTrue(hasModel(name));
        //
        try {
            return (IObjectMethodCallsNet) pool.borrowObject(name);
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    public void releaseModel(final IObjectMethodCallsNet callsNet) {
        try {
            pool.returnObject(callsNet.getType(), callsNet);
        } catch (final Exception e) {
            Throws.throwUnhandledException(e);
        }
    }

    public Set<ITypeName> findTypesBySimpleName(final ITypeName simpleName) {
        Preconditions.checkArgument("".equals(simpleName.getPackage().getIdentifier()));
        readSupportedTypes();
        final Set<ITypeName> types = Sets.newHashSet();
        final String expectedClassName = simpleName.getClassName().substring(1);
        for (final ITypeName supportedType : supportedTypes) {
            if (supportedType.getClassName().equals(expectedClassName)) {
                types.add(supportedType);
            }
        }
        return types;
    }

    protected IObjectMethodCallsNet loadNetwork(final ITypeName name) {
        try {
            final BayesianNetwork network = loader.loadNetworkForTypeName(name, BayesianNetwork.class);
            return new BayesNetWrapper(name, network);
        } catch (final IOException x) {
            throw throwUnhandledException(x);
        }
    }

    private class CallsModelPoolFactory implements KeyedPoolableObjectFactory {
        @Override
        public boolean validateObject(final Object arg0, final Object arg1) {
            return true;
        }

        @Override
        public void passivateObject(final Object arg0, final Object arg1) throws Exception {
        }

        @Override
        public Object makeObject(final Object key) throws Exception {
            return loadNetwork((ITypeName) key);
        }

        @Override
        public void destroyObject(final Object arg0, final Object arg1) throws Exception {
        }

        @Override
        public void activateObject(final Object typeName, final Object callsNet) throws Exception {
            ((IObjectMethodCallsNet) callsNet).clearEvidence();
        }
    }

}
