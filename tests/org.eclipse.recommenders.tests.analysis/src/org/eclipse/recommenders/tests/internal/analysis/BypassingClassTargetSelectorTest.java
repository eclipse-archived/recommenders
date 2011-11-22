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

import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createCGNodeMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createNewSiteMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockCGNodeGetClassHierarchy;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockNewSiteGetDeclaredType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.internal.analysis.selectors.BypassingAbstractClassesClassTargetSelector;
import org.eclipse.recommenders.tests.wala.BundleClassloaderBasedClassHierarchy;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.BypassSyntheticClass;
import com.ibm.wala.types.TypeReference;

public class BypassingClassTargetSelectorTest {

    private static IClassHierarchy cha = BundleClassloaderBasedClassHierarchy
            .newInstance(BypassingAbstractClassesClassTargetSelector.class);
    private final BypassingAbstractClassesClassTargetSelector sut = new BypassingAbstractClassesClassTargetSelector();

    private NewSiteReference site;

    private CGNode caller;

    @Test
    public void testGetAllocatedTarget_Primitive() {
        // setup
        setupSiteAndCGNode(TypeReference.Boolean);
        // exercise
        final IClass actual = sut.getAllocatedTarget(caller, site);
        // verify
        assertEquals(null, actual);
    }

    @Test
    public void testGetAllocatedTarget_Primordial_JavaLangCharacter() {
        // setup
        setupSiteAndCGNode(TypeReference.JavaLangCharacter);
        // exercise
        final IClass allocated = sut.getAllocatedTarget(caller, site);
        // verify
        assertEquals(TypeReference.JavaLangCharacter, allocated.getReference());
    }

    @Test
    public void testGetAllocatedTarget_Interface_JavaLangSet() {
        // setup
        setupSiteAndCGNode(TypeReference.JavaUtilSet);
        // exercise
        final IClass allocated = sut.getAllocatedTarget(caller, site);
        // verify
        assertTrue(allocated instanceof BypassSyntheticClass);
        final IClass realType = ((BypassSyntheticClass) allocated).getRealType();
        assertEquals(TypeReference.JavaUtilSet, realType.getReference());
    }

    private void setupSiteAndCGNode(final TypeReference declaredNewSiteType) {
        site = createNewSiteMock();
        caller = createCGNodeMock();

        mockNewSiteGetDeclaredType(site, declaredNewSiteType);
        mockCGNodeGetClassHierarchy(caller, cha);
    }
}
