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

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import names.Names__Field_To_Temp_Local;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.recommenders.internal.commons.analysis.utils.LocalNamesCollectorNaiveEdition;
import org.eclipse.recommenders.tests.commons.analysis.utils.BundleClassloaderBasedClassHierarchy;
import org.eclipse.recommenders.tests.commons.analysis.utils.WalaTestUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;

public class NaiveLocalNamesCollectorTest {

    private static IClassHierarchy cha = BundleClassloaderBasedClassHierarchy
            .newInstance(NaiveLocalNamesCollectorTest.class);
    private final AnalysisCache cache = new AnalysisCache();

    private IR ir;
    private IMethod method;
    private LocalNamesCollectorNaiveEdition sut;

    @Test
    public void testSimpleCase() throws InvalidClassFileException {
        setupNameCollectors(Names__Field_To_Temp_Local.class);
        verifyLocalNames("this", "tmp");
    }

    @SuppressWarnings("unchecked")
    private void verifyLocalNames(final String... expectedNames) {
        final Collection<String> expected = Lists.newArrayList(expectedNames);
        final Collection<String> actual = sut.getNames();
        final Collection<String> diff = CollectionUtils.disjunction(actual, expected);
        final String msg = format("Expected diff to be empty but found: %s", diff);
        assertTrue(msg, diff.isEmpty());

    }

    private void setupNameCollectors(final Class<?> testCase) throws InvalidClassFileException {
        method = WalaTestUtils.lookupTestMethod(cha, testCase);
        ir = cache.getIR(method);
        sut = new LocalNamesCollectorNaiveEdition(ir);
    }

}
