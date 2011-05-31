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
package org.eclipse.recommenders.tests.rcp.codecompletion.calls.unit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.ICallsModelLoader;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.ICallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Injector;

public class BayesianNetworkModelTest {

    @Test
    @Ignore
    public void testObservations() {
        final Injector injector = InjectionService.getInstance().getInjector();
        final ICallsModelStore modelStore = injector.getInstance(CallsModelStore.class);
        final ICallsModelLoader loader = injector.getInstance(ICallsModelLoader.class);

        final Set<ITypeName> availableTypes = loader.readAvailableTypes();
        for (final ITypeName type : availableTypes) {
            testObservations(modelStore.getModel(type));
        }
    }

    private void testObservations(final IObjectMethodCallsNet model) {
        final Collection<IMethodName> methods = model.getMethodCalls();
        for (final IMethodName method : methods) {
            final HashSet<IMethodName> observedCalls = new HashSet<IMethodName>();
            observedCalls.add(method);

            model.clearEvidence();
            model.setObservedMethodCalls(null, observedCalls);
            final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = model.getRecommendedMethodCalls(0.0);
            Assert.assertTrue(Collections.disjoint(recommendedMethodCalls, observedCalls));
        }
    }
}
