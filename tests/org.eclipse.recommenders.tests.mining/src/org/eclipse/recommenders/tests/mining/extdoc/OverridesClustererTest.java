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
package org.eclipse.recommenders.tests.mining.extdoc;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.extdoc.MethodPattern;
import org.eclipse.recommenders.mining.extdocs.OverridesClusterer;
import org.eclipse.recommenders.utils.Bag;
import org.eclipse.recommenders.utils.HashBag;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class OverridesClustererTest {

    private static final IMethodName METHOD_A = VmMethodName.get("Test.a()V");
    private static final IMethodName METHOD_B = VmMethodName.get("Test.b()V");
    private static final IMethodName METHOD_C = VmMethodName.get("Test.c()V");
    private static final IMethodName METHOD_INIT = VmMethodName.get("Test.<init>()V");

    @Test
    public void testHappyPath() {
        final OverridesClusterer sut = new OverridesClusterer(1);
        final Bag<Set<IMethodName>> rawData = createRawDataBag(createOverridesSet(METHOD_A));
        final List<MethodPattern> methodPatterns = sut.cluster(rawData);

        assertEquals(1, methodPatterns.size());
        assertEquals(createMethodPattern(1, Tuple.create(METHOD_A, 1.0)), methodPatterns.get(0));
    }

    @Test
    public void testMinClusterSize() {
        final OverridesClusterer sut = new OverridesClusterer(2);
        final Bag<Set<IMethodName>> rawData = createRawDataBag(createOverridesSet(METHOD_A),
                createOverridesSet(METHOD_A, METHOD_B), createOverridesSet(METHOD_C));
        final List<MethodPattern> methodPatterns = sut.cluster(rawData);

        assertEquals(1, methodPatterns.size());
        assertEquals(createMethodPattern(2, Tuple.create(METHOD_A, 1.0), Tuple.create(METHOD_B, 0.5)),
                methodPatterns.get(0));
    }

    @Test
    public void testMultipleOccurenceOfPattern() {
        final OverridesClusterer sut = new OverridesClusterer(1);
        final Bag<Set<IMethodName>> rawData = createRawDataBag(createOverridesSet(METHOD_A),
                createOverridesSet(METHOD_A));
        final List<MethodPattern> methodPatterns = sut.cluster(rawData);

        assertEquals(1, methodPatterns.size());
        assertEquals(createMethodPattern(2, Tuple.create(METHOD_A, 1.0)), methodPatterns.get(0));
    }

    @Test
    public void testIgnoreInit() {
        final OverridesClusterer sut = new OverridesClusterer(1);
        final Bag<Set<IMethodName>> rawData = createRawDataBag(createOverridesSet(METHOD_A),
                createOverridesSet(METHOD_INIT));
        final List<MethodPattern> methodPatterns = sut.cluster(rawData);

        assertEquals(1, methodPatterns.size());
        assertEquals(createMethodPattern(1, Tuple.create(METHOD_A, 1.0)), methodPatterns.get(0));
    }

    @Test
    public void testMethodProbabilityFilter() {
        final OverridesClusterer sut = new OverridesClusterer(1);
        final Bag<Set<IMethodName>> rawData = createRawDataBag(createOverridesSet(METHOD_A),
                createOverridesSet(METHOD_A), createOverridesSet(METHOD_A), createOverridesSet(METHOD_A),
                createOverridesSet(METHOD_A, METHOD_B));
        final List<MethodPattern> methodPatterns = sut.cluster(rawData);

        assertEquals(1, methodPatterns.size());
        assertEquals(createMethodPattern(5, Tuple.create(METHOD_A, 1.0)), methodPatterns.get(0));
    }

    private MethodPattern createMethodPattern(final int numberOfObservations,
            final Tuple<IMethodName, Double>... methods) {
        final Map<IMethodName, Double> methodMap = Maps.newHashMap();
        for (final Tuple<IMethodName, Double> tuple : methods) {
            methodMap.put(tuple.getFirst(), tuple.getSecond());
        }
        return MethodPattern.create(numberOfObservations, methodMap);
    }

    private Set<IMethodName> createOverridesSet(final IMethodName... methodNames) {
        final Set<IMethodName> res = Sets.newHashSet();
        for (final IMethodName methodName : methodNames) {
            res.add(methodName);
        }
        return res;
    }

    private Bag<Set<IMethodName>> createRawDataBag(final Set<IMethodName>... overrides) {
        final Bag<Set<IMethodName>> res = HashBag.newHashBag();
        for (final Set<IMethodName> set : overrides) {
            res.add(set);
        }
        return res;
    }
}
