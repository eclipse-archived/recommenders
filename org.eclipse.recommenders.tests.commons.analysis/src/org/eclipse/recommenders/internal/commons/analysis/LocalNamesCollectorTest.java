/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.commons.analysis;

import java.util.Collection;

import names.Names__Alias01;
import names.Names__Alias02;
import names.Names__Field_Constructor;
import names.Names__Field_Method_Uninitialized;
import names.Names__Field_To_Temp_Local;
import names.Names__Local_Constructor;
import names.Names__Local_Defined_By_Constructor;
import names.Names__Local_Defined_By_ReturnValue;
import names.Names__Local_Method;
import names.Names__Local_With_Branch;
import names.Names__MultiNames;
import names.Names__Param_Method_Call;

import org.eclipse.recommenders.internal.commons.analysis.utils.LocalNamesCollector;
import org.eclipse.recommenders.tests.commons.analysis.utils.BundleClassloaderBasedClassHierarchy;
import org.eclipse.recommenders.tests.commons.analysis.utils.WalaTestUtils;
import org.junit.Test;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;

public class LocalNamesCollectorTest {

    private static IClassHierarchy cha = BundleClassloaderBasedClassHierarchy
            .newInstance(LocalNamesCollectorTest.class);
    private final AnalysisCache cache = new AnalysisCache();
    private LocalNamesCollector reference;
    // private LocalNamesCollector2 sut;
    private final Class<?>[] testcases = new Class[] { Names__Field_To_Temp_Local.class, Names__Alias01.class,
            Names__MultiNames.class, Names__Alias02.class, Names__Field_Constructor.class,
            Names__Field_Method_Uninitialized.class, Names__Local_Constructor.class,
            Names__Local_Defined_By_Constructor.class, Names__Local_Defined_By_ReturnValue.class,
            Names__Local_Method.class, Names__Local_With_Branch.class, Names__Param_Method_Call.class, };
    private IR ir;

    @Test
    public void testSameResults() throws Exception {
        for (final Class<?> clazz : testcases) {
            testSameResults(clazz);
        }
    }

    private void testSameResults(final Class clazz) {
        setupNameCollectors(clazz);
        verifySameNamesAndValues();
    }

    private void setupNameCollectors(final Class<?> testCase) {
        final IMethod method = WalaTestUtils.lookupTestMethod(cha, testCase);
        ir = cache.getIR(method);
        reference = new LocalNamesCollector(ir);
        // sut = new LocalNamesCollector2(ir);
    }

    private void verifySameNamesAndValues() {
        verifySameNames();
        verifySameValues();
        for (final int value : reference.getValues()) {
            // final String actual = sut.getName(value);
            final String expected = reference.getName(value);
            // assertEquals(expected, actual);
        }
    }

    private void verifySameNames() {
        final Collection<String> expected = reference.getNames();
        // final Collection<String> actual = sut.getNames();
        // final Collection diff = CollectionUtils.disjunction(actual,
        // expected);
        // assertTrue(diff + " not empty", diff.isEmpty());
    }

    private void verifySameValues() {
        final Collection<Integer> expected = reference.getValues();
        // final Collection<Integer> actual = sut.getValues();
        // final Collection diff = CollectionUtils.disjunction(actual,
        // expected);
        // assertTrue(diff + " not empty", diff.isEmpty());
    }
}
