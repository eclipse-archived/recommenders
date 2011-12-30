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
package org.eclipse.recommenders.tests.internal.analysis;

import static java.lang.String.format;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.recommenders.internal.analysis.analyzers.CallGraphMethodAnalyzer;
import org.eclipse.recommenders.internal.analysis.analyzers.ICallGraphAnalyzer;
import org.eclipse.recommenders.internal.analysis.analyzers.ParameterCallsitesCallGraphAnalyzer;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.entrypoints.RecommendersEntrypoint;
import org.eclipse.recommenders.internal.analysis.utils.InstanceCallGraphBuilder;
import org.eclipse.recommenders.tests.wala.BundleClassloaderBasedClassHierarchy;
import org.eclipse.recommenders.tests.wala.WalaTestUtils;
import org.junit.Before;
import org.junit.Test;

import tracing.Tracing__Call_From_Inner_Class_To_Private_Method_Of_Enclosing_Class.InnerClass;
import tracing.Tracing__Call_From_Subtype_To_Framework_Supertype;
import tracing.Tracing__Calls_To_Several_Locals_Using_Delegate_Method;
import tracing.Tracing__Hierarchy_Subclass;
import tracing.Tracing__Recursive_Calls_To_This;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.inject.Provider;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class ParameterCallsitesTracingTest {
    private static IClassHierarchy cha = BundleClassloaderBasedClassHierarchy
            .newInstance(ParameterCallsitesTracingTest.class);

    private AnalysisOptions opts;

    private AnalysisCache cache;

    private InstanceCallGraphBuilder cgBuilder;

    private MethodDeclaration output;

    private CallGraphMethodAnalyzer sut;

    @Before
    public void setup() {
        opts = new AnalysisOptions();
        opts.setAnalysisScope(cha.getScope());
        cache = new AnalysisCache();
        cgBuilder = new InstanceCallGraphBuilder(opts, cache, WalaTestUtils.getNativeSummaries(cha));
        output = MethodDeclaration.create();
        final Set<ICallGraphAnalyzer> analyzers = Sets.newHashSet();
        analyzers.add(new ParameterCallsitesCallGraphAnalyzer());
        sut = new CallGraphMethodAnalyzer(new Provider<InstanceCallGraphBuilder>() {
            @Override
            public InstanceCallGraphBuilder get() {
                return cgBuilder;
            }
        }, analyzers);
    }

    @Test
    public void testCallFromInner2EnclosingClass() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(InnerClass.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkParameterCallSitesCount(output, 1);
    }

    @Test
    public void testRecursiveCallsOnThis() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Recursive_Calls_To_This.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkParameterCallSitesCount(output, 1/* s1 */, 1/* s2 */, 1/* s3 */);
    }

    @Test
    public void testCallFromSubtypeToFrameworkSupertype() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Call_From_Subtype_To_Framework_Supertype.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkParameterCallSitesCount(output, 1);
    }

    @Test
    public void testCallsToLocalInHierarchy() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Hierarchy_Subclass.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkParameterCallSitesCount(output, 3);
    }

    @Test
    public void testCallsToSeveralSameTypeLocalsUsingDelegateMethod() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Several_Locals_Using_Delegate_Method.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkParameterCallSitesCount(output, 1, 1, 1);
    }

    private void exerciseSUTAndFilterValues(final RecommendersEntrypoint entrypoint) {
        exerciseSUT(entrypoint);
        filterValuesWithEmtpyParameterCallsites();
    }

    private void exerciseSUT(final RecommendersEntrypoint entrypoint) {
        sut.analyzeMethod(entrypoint, output, new NullProgressMonitor());
    }

    private void filterValuesWithEmtpyParameterCallsites() {
        for (final Iterator<ObjectInstanceKey> it = output.objects.iterator(); it.hasNext();) {
            final ObjectInstanceKey value = it.next();
            if (value.parameterCallSites.isEmpty()) {
                it.remove();
            }
        }
    }

    private RecommendersEntrypoint lookupEntrypoint(final Class<?> testCase) {
        final IMethod m = WalaTestUtils.lookupTestMethod(cha, testCase);
        final RecommendersEntrypoint e = new RecommendersEntrypoint(m);
        return e;
    }

    private void checkParameterCallSitesCount(final MethodDeclaration output,
            final int... pExpectedParameterCallsitesCount) {
        final List<Integer> expectedReceiverCallsitesCount = Lists.newLinkedList(Ints
                .asList(pExpectedParameterCallsitesCount));
        for (final ObjectInstanceKey value : output.objects) {
            final Integer actualCount = value.parameterCallSites.size();
            final boolean foundCountInExpected = expectedReceiverCallsitesCount.remove(actualCount);
            if (!foundCountInExpected) {
                final String failMessage = format("actual count '%d' for '%s' could not be found (anymore?)",
                        actualCount, value);
                fail(failMessage);
            }
        }
        if (!expectedReceiverCallsitesCount.isEmpty()) {
            final String failMessage = format("expected '%d' more traces with counts '%s'.",
                    expectedReceiverCallsitesCount.size(), expectedReceiverCallsitesCount);
            fail(failMessage);
        }
    }
}
