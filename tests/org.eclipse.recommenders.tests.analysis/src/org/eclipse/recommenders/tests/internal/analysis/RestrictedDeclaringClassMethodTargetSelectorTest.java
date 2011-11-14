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

import static org.eclipse.recommenders.tests.analysis.TestConstants.METHOD_CLASS_FOR_NAME;
import static org.eclipse.recommenders.tests.analysis.TestConstants.METHOD_OBJECT_HASHCODE;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createCGNodeMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createCallGraphBuilderMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createCallSiteReferenceMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPublicStaticMethodMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.mockCallSiteGetDeclaredTarget;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.mockCallSiteIsDispatch;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.mockCallSiteIsFixed;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.recommenders.internal.analysis.selectors.RestrictedDeclaringClassMethodTargetSelector;
import org.eclipse.recommenders.tests.analysis.BundleClassloaderBasedClassHierarchy;
import org.junit.Test;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class RestrictedDeclaringClassMethodTargetSelectorTest {

    private static IClassHierarchy cha = BundleClassloaderBasedClassHierarchy
            .newInstance(RestrictedDeclaringClassMethodTargetSelectorTest.class);

    private RestrictedDeclaringClassMethodTargetSelector sut;

    private IClass thisClass;

    @Test(expected = IllegalArgumentException.class)
    public void testGetCalleeTarget_CallToClassForName() {
        setupSUT();
        final CallSiteReference call = createCallSiteReferenceMock();
        mockCallSiteGetDeclaredTarget(call, METHOD_CLASS_FOR_NAME);
        mockCallSiteIsFixed(call, true);
        sut.getCalleeTarget(createCGNodeMock(), call, thisClass);
        // "calls to java.lang.class#forName should not be resolved",
    }

    @Test
    public void testGetCalleeTarget_CallToObjectHashCode() {
        setupSUT();
        final CallSiteReference call = createCallSiteReferenceMock();
        mockCallSiteGetDeclaredTarget(call, METHOD_OBJECT_HASHCODE);
        mockCallSiteIsDispatch(call, true);
        final CGNode cgNode = createCGNodeMock();
        IMethod sourceMethod = createPublicStaticMethodMock();
        when(cgNode.getMethod()).thenReturn(sourceMethod);
        final IMethod actualCallTarget = sut.getCalleeTarget(cgNode, call, thisClass);
        assertEquals(METHOD_OBJECT_HASHCODE, actualCallTarget.getReference());
    }

    private void setupSUT() {

        final ClassHierarchyMethodTargetSelector delegate = new ClassHierarchyMethodTargetSelector(cha);
        thisClass = cha.getRootClass();
        final SSAPropagationCallGraphBuilder builder = createCallGraphBuilderMock();
        sut = new RestrictedDeclaringClassMethodTargetSelector(delegate, thisClass, builder);
    }
}
