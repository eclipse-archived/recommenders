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

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

@Ignore
public class NetworksRandomLoadTests {

    private static Set<ITypeName> availableTypes;
    private static ICallsModelStore modelStore;

    @BeforeClass
    public static void init() {
        final Injector injector = InjectionService.getInstance().getInjector();
        final ICallsModelLoader loader = injector.getInstance(ICallsModelLoader.class);
        modelStore = injector.getInstance(CallsModelStore.class);
        availableTypes = loader.readAvailableTypes();
    }

    @Test
    public void testNullArguments() {
        for (final ITypeName type : availableTypes) {
            try {
                final IObjectMethodCallsNet model = modelStore.getModel(type);
                model.clearEvidence();
                model.setMethodContext(null);
                model.setCalled(null);
                model.setPattern(null);
                model.updateBeliefs();
                model.getRecommendedMethodCalls(0.0);
            } catch (final Exception e) {
                throwUnhandledException("error during setting null values tests in model " + type, e);
            }
        }

    }

    @Test
    // @Ignore
    public void testObservations() {
        int numberOfModelsChecked = 0;
        for (final ITypeName type : availableTypes) {
            numberOfModelsChecked++;
            if (numberOfModelsChecked % 1000 == 0) {
                System.out.printf("Models checked: %d\n", numberOfModelsChecked);
            }
            try {
                final IObjectMethodCallsNet model = modelStore.getModel(type);
                testRandomlyObserveContextsAndPatterns(model);
                testObserveAllMethodCallsOneByOne(model);
            } catch (final Exception e) {
                throwUnhandledException("error during RANDOM tests in model " + type, e);
            }
        }
    }

    private void testRandomlyObserveContextsAndPatterns(final IObjectMethodCallsNet model) {
        final Collection<IMethodName> randomContexts = randomSubset(model.getContexts(), 3);
        for (final IMethodName callingContext : randomContexts) {
            final Collection<String> randomPatterns = randomSubset(model.getPatterns(), 3);
            for (final String patternName : randomPatterns) {
                model.clearEvidence();
                model.setMethodContext(callingContext);
                model.setPattern(patternName);
                model.updateBeliefs();
                model.getRecommendedMethodCalls(0.0);
            }
        }
    }

    private <T> Collection<T> randomSubset(final Collection<T> elements, final int maxSize) {
        final List<T> l = Lists.newLinkedList(elements);
        Collections.shuffle(l);
        final List<T> subList = l.subList(0, Math.min(maxSize, elements.size()));
        return subList;
    }

    private void testObserveAllMethodCallsOneByOne(final IObjectMethodCallsNet model) {
        final Collection<IMethodName> methods = model.getMethodCalls();
        for (final IMethodName method : methods) {
            final HashSet<IMethodName> observedCalls = new HashSet<IMethodName>();
            observedCalls.add(method);

            model.clearEvidence();
            model.setObservedMethodCalls(null, observedCalls);
            model.updateBeliefs();
            final Set<IMethodName> recommendedMethodCalls = getMethods(model.getRecommendedMethodCalls(0.0));

            Assert.assertTrue(Collections.disjoint(recommendedMethodCalls, observedCalls));
        }
    }

    private Set<IMethodName> getMethods(final SortedSet<Tuple<IMethodName, Double>> recommendations) {
        final Set<IMethodName> res = Sets.newHashSet();
        for (final Tuple<IMethodName, Double> pair : recommendations) {
            res.add(pair.getFirst());
        }
        return res;
    }
}
