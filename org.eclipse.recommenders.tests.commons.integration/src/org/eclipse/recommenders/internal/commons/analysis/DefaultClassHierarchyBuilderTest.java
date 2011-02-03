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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultAnalysisScopeBuilder;
import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultClassHierarchyBuilder;
import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisFixture;
import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisScopeBuilder;
import org.eclipse.recommenders.tests.commons.analysis.utils.JREOnlyAnalysisFixture;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

public class DefaultClassHierarchyBuilderTest {

    @Test
    public void testBuildPrimordialClassHierarchy() throws ClassHierarchyException {
        // setup
        final int LEAST_NUMBER_OF_JDK_CLASSES = 1000;
        final IAnalysisFixture fixture = JREOnlyAnalysisFixture.create();
        final IAnalysisScopeBuilder scopeBuilder = DefaultAnalysisScopeBuilder.buildFromFixture(fixture);
        final AnalysisScope scope = scopeBuilder.getAnalysisScope();
        final DefaultClassHierarchyBuilder sut = new DefaultClassHierarchyBuilder(scope);
        //
        // exercise
        final IClassHierarchy cha = sut.getClassHierachy();
        System.out.println("number of classes: " + cha.getNumberOfClasses());
        //
        // verify
        assertTrue(cha.getNumberOfClasses() > LEAST_NUMBER_OF_JDK_CLASSES);
        for (final IClass clazz : cha) {
            assertEquals(clazz.getClassLoader().getReference(), ClassLoaderReference.Primordial);
        }
    }
}
