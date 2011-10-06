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
package org.eclipse.recommenders.mining.extdoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.mining.extdocs.MethodSelfcallDirectivesGenerator;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;
import org.junit.Test;

import com.google.common.collect.Lists;

public class MethodSelfcallDirectivesGeneratorTest {

    private final ITypeName superclass = VmTypeName.get("LSuperclass");
    private final IMethodName overridenMethod1 = VmMethodName.get(superclass.getIdentifier(), "o1()V");
    private final IMethodName overridenMethod2 = VmMethodName.get(superclass.getIdentifier(), "o2()V");

    private final ITypeName testclass = VmTypeName.get("LTestclass");
    private final IMethodName m1 = VmMethodName.get(testclass.getIdentifier(), "m1()V");
    private final IMethodName m2 = VmMethodName.get(testclass.getIdentifier(), "m2()V");
    private final IMethodName m3 = VmMethodName.get(testclass.getIdentifier(), "m3()V");

    @Test
    public void testHappyPath() {
        final Iterable<CompilationUnit> cus = Lists.newArrayList(createCompilationUnit("this", overridenMethod1, m1));
        final List<MethodSelfcallDirectives> selfCalls = createSut(0.05, cus);

        final MethodSelfcallDirectives selfCall = assertOneSelfcall(selfCalls);
        assertEquals(overridenMethod1, selfCall.getMethod());
        assertEquals(1, selfCall.getNumberOfDefinitions());
        assertAmountOfCalls(m1, 1, selfCall);
        assertAmountOfCalls(m2, 0, selfCall);
    }

    @Test
    public void testMinProbabilityThreshold() {
        final Iterable<CompilationUnit> cus = Lists.newArrayList(
                createCompilationUnit("this", overridenMethod1, m1, m2),
                createCompilationUnit("this", overridenMethod1, m1),
                createCompilationUnit("this", overridenMethod2, m3), createCompilationUnit("this", overridenMethod2));
        final List<MethodSelfcallDirectives> selfCalls = createSut(0.6, cus);

        final MethodSelfcallDirectives selfCall = assertOneSelfcall(selfCalls);
        assertEquals(overridenMethod1, selfCall.getMethod());
        assertEquals(2, selfCall.getNumberOfDefinitions());
        assertAmountOfCalls(m1, 2, selfCall);
        assertAmountOfCalls(m2, 0, selfCall);
        assertAmountOfCalls(m3, 0, selfCall);
    }

    @Test
    public void testFilterEmptyCalls() {
        final Iterable<CompilationUnit> cus = Lists.newArrayList(createCompilationUnit("this", overridenMethod1));
        final List<MethodSelfcallDirectives> selfCalls = createSut(0.05, cus);
        assertEquals(0, selfCalls.size());
    }

    @Test
    public void testIgnoreNonSelfcalls() {
        final Iterable<CompilationUnit> cus = Lists
                .newArrayList(createCompilationUnit("someName", overridenMethod1, m1));
        final List<MethodSelfcallDirectives> selfCalls = createSut(0.05, cus);
        assertEquals(0, selfCalls.size());
    }

    @Test
    public void testIgnoreSelfcallsWithoutCtx() {
        final Iterable<CompilationUnit> cus = Lists.newArrayList(createCompilationUnit("someName", null, m1));
        final List<MethodSelfcallDirectives> selfCalls = createSut(0.05, cus);
        assertEquals(0, selfCalls.size());
    }

    private List<MethodSelfcallDirectives> createSut(final double minProbabilityThreshold,
            final Iterable<CompilationUnit> cus) {
        final MethodSelfcallDirectivesGenerator sut = new MethodSelfcallDirectivesGenerator(minProbabilityThreshold);
        sut.initialize();
        sut.analyzeCompilationUnits(cus);
        return sut.generate();
    }

    private MethodSelfcallDirectives assertOneSelfcall(final List<MethodSelfcallDirectives> selfCalls) {
        assertEquals(1, selfCalls.size());
        return selfCalls.get(0);
    }

    private void assertAmountOfCalls(final IMethodName method, final int amount, final MethodSelfcallDirectives selfCall) {
        final Map<IMethodName, Integer> calls = selfCall.getCalls();
        if (amount == 0) {
            assertFalse(calls.containsKey(method));
        } else {
            assertTrue(calls.containsKey(method));
            assertEquals(amount, (int) calls.get(method));
        }
    }

    private CompilationUnit createCompilationUnit(final String receiverName, final IMethodName overridenMethod,
            final IMethodName... selfCalls) {
        final CompilationUnit cu = CompilationUnit.create();
        cu.primaryType = TypeDeclaration.create(testclass, superclass);
        final MethodDeclaration methodDecl = createMethodDeclaration(receiverName, overridenMethod, selfCalls);
        cu.primaryType.methods.add(methodDecl);
        return cu;
    }

    private MethodDeclaration createMethodDeclaration(final String receiverName, final IMethodName overridenMethod,
            final IMethodName... selfCalls) {
        final MethodDeclaration methodDecl = MethodDeclaration.create();
        methodDecl.firstDeclaration = overridenMethod;
        final ObjectInstanceKey objInstanceKey = ObjectInstanceKey.create(testclass, Kind.FIELD);
        objInstanceKey.names.add(receiverName);
        methodDecl.objects.add(objInstanceKey);

        for (final IMethodName selfCall : selfCalls) {
            final ReceiverCallSite receiverCallSite = ReceiverCallSite.create(receiverName, selfCall, overridenMethod,
                    0);
            objInstanceKey.receiverCallSites.add(receiverCallSite);
        }
        return methodDecl;
    }
}
