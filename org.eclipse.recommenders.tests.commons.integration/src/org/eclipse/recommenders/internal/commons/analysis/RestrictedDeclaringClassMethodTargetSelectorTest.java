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
        CallSiteReference call = createCallSiteReferenceMock();
        mockCallSiteGetDeclaredTarget(call, METHOD_CLASS_FOR_NAME);
        mockCallSiteIsFixed(call, true);
        sut.getCalleeTarget(createCGNodeMock(), call, thisClass);
        // "calls to java.lang.class#forName should not be resolved",
    }

    @Test
    public void testGetCalleeTarget_CallToObjectClone() {
        setupSUT();
        CallSiteReference call = createCallSiteReferenceMock();
        mockCallSiteGetDeclaredTarget(call, METHOD_OBJECT_HASHCODE);
        mockCallSiteIsDispatch(call, true);
        IMethod actualCallTarget = sut.getCalleeTarget(createCGNodeMock(), call, thisClass);
        assertEquals(METHOD_OBJECT_HASHCODE, actualCallTarget.getReference());
    }

    private void setupSUT() {
        IClassHierarchy cha = JREOnlyClassHierarchyFixture.getInstance();
        ClassHierarchyMethodTargetSelector delegate = new ClassHierarchyMethodTargetSelector(cha);
        thisClass = cha.getRootClass();
        SSAPropagationCallGraphBuilder builder = createCallGraphBuilderMock();
        sut = new RestrictedDeclaringClassMethodTargetSelector(delegate, thisClass, builder);
    }
}
