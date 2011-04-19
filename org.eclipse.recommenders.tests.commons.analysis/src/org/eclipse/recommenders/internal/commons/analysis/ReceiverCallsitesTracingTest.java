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
package org.eclipse.recommenders.internal.commons.analysis;

import static junit.framework.Assert.fail;
import static junit.framework.Assert.format;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.CallGraphMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICallGraphAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ReceiverCallsitesCallGraphAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.entrypoints.RecommendersEntrypoint;
import org.eclipse.recommenders.internal.commons.analysis.utils.InstanceCallGraphBuilder;
import org.eclipse.recommenders.tests.commons.analysis.utils.BundleClassloaderBasedClassHierarchy;
import org.eclipse.recommenders.tests.commons.analysis.utils.WalaTestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import tracing.StaticFactoryMethod;
import tracing.Tracing__Call_From_Inner_Class_To_Private_Method_Of_Enclosing_Class.InnerClass;
import tracing.Tracing__Call_From_Subtype_To_Framework_Supertype;
import tracing.Tracing__Call_To_Local_Non_Framework_Type;
import tracing.Tracing__Call_To_New_Integer;
import tracing.Tracing__Call_To_Static_Framework_Method;
import tracing.Tracing__Calls_To_Field_With_Init;
import tracing.Tracing__Calls_To_Field_Without_Init;
import tracing.Tracing__Calls_To_Local_With_Init;
import tracing.Tracing__Calls_To_Local_With_Init_In_Delegate;
import tracing.Tracing__Calls_To_Parameter;
import tracing.Tracing__Calls_To_Private_Return_Value_Is_Unitialized_Field;
import tracing.Tracing__Calls_To_Private_Return_Value_With_Init;
import tracing.Tracing__Calls_To_Several_Locals_Using_Delegate_Method;
import tracing.Tracing__Calls_To_Several_Locals_Using_Two_Delegate_Methods;
import tracing.Tracing__Calls_To_Several_Parameters;
import tracing.Tracing__Collects_Duplicated_Calls;
import tracing.Tracing__Hierarchy_Subclass;
import tracing.Tracing__Hierarchy_Subclass_Call_To_CreatorMethod;
import tracing.Tracing__InterfaceUsages;
import tracing.Tracing__Recursive_Calls_To_This;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.inject.Provider;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class ReceiverCallsitesTracingTest {
    private static IClassHierarchy cha = BundleClassloaderBasedClassHierarchy
            .newInstance(ReceiverCallsitesTracingTest.class);

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
        analyzers.add(new ReceiverCallsitesCallGraphAnalyzer());
        sut = new CallGraphMethodAnalyzer(new Provider<InstanceCallGraphBuilder>() {
            @Override
            public InstanceCallGraphBuilder get() {
                return cgBuilder;
            }
        }, analyzers);
    }

    @Test
    public void testCallToNewInteger() {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Call_To_New_Integer.class);
        // exercise
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1);
    }

    @Test
    public void testCallToInterface() {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__InterfaceUsages.class);
        // exercise
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 2);
    }

    @Test
    @Ignore
    public void testStaticFactoryMethod() {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(StaticFactoryMethod.class);
        // exercise
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1);
    }

    @Test
    public void testCallFromInner2EnclosingClass() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(InnerClass.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1/* access$0 */, 1);
    }

    @Test
    public void testRecursiveCallsOnThis() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Recursive_Calls_To_This.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1/* this */, 1/* s2 */);
    }

    @Test
    public void testCallsToFieldWithConstructorCall() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Field_With_Init.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 4);
    }

    @Test
    public void testCallFromSubtypeToFrameworkSupertype() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Call_From_Subtype_To_Framework_Supertype.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1);
    }

    @Test
    public void testCallsToFieldWithoutConstructorCall() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Field_Without_Init.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 3);
    }

    @Test
    @Ignore("call graph construction does not include inlining of statc methods anymore. Need a test that checks for definition sites instead")
    public void testCallToStaticFrameworkMethod() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Call_To_Static_Framework_Method.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1);
    }

    @Test
    @Ignore("this test is not required anymore since call graph does not include super calls anymore")
    public void testCallsToLocalInHierarchy() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Hierarchy_Subclass.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 3/* this */, 4 /* super value */);
    }

    @Test
    public void testCallsToLocalWithInit() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Local_With_Init.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 4);
    }

    @Test
    public void testCallsToLocalWithInitInDelegate() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Local_With_Init_In_Delegate.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1/* this */, 4/* button */);
    }

    @Test
    public void testCallsToParameter() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Parameter.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 2);
    }

    @Test
    @Ignore("This test is not suitable for ide 2.0 since no NonFramework types exist anymore")
    public void testCallToLocalNonFrameworkType() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Call_To_Local_Non_Framework_Type.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, new int[0]);
    }

    @Test
    public void testCallsToPrivateReturnValueIsUnitializedField() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Private_Return_Value_Is_Unitialized_Field.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1/* this */, 2 /* button */);
    }

    /**
     * This method has a changed behavior in v2 since it does not follow calls
     * to the super class anymore. At least not now.
     */
    @Test
    public void testCallsToCreatorMethodInSuperclass() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Hierarchy_Subclass_Call_To_CreatorMethod.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1, 1);
    }

    @Test
    public void testCallsToPrivateReturnValueWithInit() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Private_Return_Value_With_Init.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1/* this */, 3/* button */);
    }

    @Test
    public void testCallsToSeveralParameters() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Several_Parameters.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 1, 2, 3);
    }

    @Test
    public void testCallsToSeveralSameTypeLocalsUsingDelegateMethod() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Several_Locals_Using_Delegate_Method.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 3/* this */, 2, 3, 4);
    }

    @Test
    public void testCallsToSeveralSameTypeLocalsUsingTwoDelegateMethods() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Calls_To_Several_Locals_Using_Two_Delegate_Methods.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 4/* this */, 2, 3, 4);
    }

    @Test
    public void testCollectsDuplicatedCalls() throws Exception {
        // setup
        final RecommendersEntrypoint entrypoint = lookupEntrypoint(Tracing__Collects_Duplicated_Calls.class);
        exerciseSUTAndFilterValues(entrypoint);
        // verify
        checkReceiversCallSitesCount(output, 5);
    }

    private void exerciseSUTAndFilterValues(final RecommendersEntrypoint entrypoint) {
        sut.analyzeMethod(entrypoint, output, new NullProgressMonitor());
        filterValuesWithEmtpyReceiverCallsites();
    }

    private void filterValuesWithEmtpyReceiverCallsites() {
        for (final Iterator<ObjectInstanceKey> it = output.objects.iterator(); it.hasNext();) {
            final ObjectInstanceKey value = it.next();
            if (value.receiverCallSites.isEmpty()) {
                it.remove();
            }
        }
    }

    private RecommendersEntrypoint lookupEntrypoint(final Class<?> testCase) {
        final IMethod m = WalaTestUtils.lookupTestMethod(cha, testCase);
        final RecommendersEntrypoint e = new RecommendersEntrypoint(m);
        return e;
    }

    private void checkReceiversCallSitesCount(final MethodDeclaration output,
            final int... pExpectedReceiverCallsitesCount) {
        final List<Integer> expectedReceiverCallsitesCount = Lists.newLinkedList(Ints
                .asList(pExpectedReceiverCallsitesCount));
        for (final ObjectInstanceKey value : output.objects) {
            final Integer actualCount = value.receiverCallSites.size();
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
