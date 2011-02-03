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

import static org.eclipse.recommenders.tests.commons.analysis.utils.TestConstants.METHOD_CLASS_FOR_NAME;
import static org.eclipse.recommenders.tests.commons.analysis.utils.TestConstants.METHOD_OBJECT_HASHCODE;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createCGNodeMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createCallGraphBuilderMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createCallSiteReferenceMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockCallSiteGetDeclaredTarget;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockCallSiteIsDispatch;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockCallSiteIsFixed;
import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.internal.commons.analysis.selectors.RestrictedDeclaringClassMethodTargetSelector;
import org.eclipse.recommenders.tests.commons.analysis.utils.JREOnlyClassHierarchyFixture;
import org.junit.Test;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class RestrictedDeclaringClassMethodTargetSelectorTest {

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
    public void testGetCalleeTarget_CallToObjectClone() {
        setupSUT();
        final CallSiteReference call = createCallSiteReferenceMock();
        mockCallSiteGetDeclaredTarget(call, METHOD_OBJECT_HASHCODE);
        mockCallSiteIsDispatch(call, true);
        final IMethod actualCallTarget = sut.getCalleeTarget(createCGNodeMock(), call, thisClass);
        assertEquals(METHOD_OBJECT_HASHCODE, actualCallTarget.getReference());
    }

    private void setupSUT() {
        final IClassHierarchy cha = JREOnlyClassHierarchyFixture.getInstance();
        final ClassHierarchyMethodTargetSelector delegate = new ClassHierarchyMethodTargetSelector(cha);
        thisClass = cha.getRootClass();
        final SSAPropagationCallGraphBuilder builder = createCallGraphBuilderMock();
        sut = new RestrictedDeclaringClassMethodTargetSelector(delegate, thisClass, builder);
    }
}
