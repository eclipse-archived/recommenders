package org.eclipse.recommenders.internal.commons.analysis;

import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createCGNodeMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createNewSiteMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockCGNodeGetClassHierarchy;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockNewSiteGetDeclaredType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.internal.commons.analysis.selectors.BypassingAbstractClassesClassTargetSelector;
import org.eclipse.recommenders.tests.commons.analysis.utils.JREOnlyClassHierarchyFixture;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.BypassSyntheticClass;
import com.ibm.wala.types.TypeReference;

public class BypassingClassTargetSelectorTest {

    private final BypassingAbstractClassesClassTargetSelector sut = new BypassingAbstractClassesClassTargetSelector();

    private NewSiteReference site;

    private CGNode caller;

    private IClassHierarchy cha;

    @Test
    public void testGetAllocatedTarget_Primitive() {
        // setup
        setupSiteAndCGNode(TypeReference.Boolean);
        // exercise
        IClass actual = sut.getAllocatedTarget(caller, site);
        // verify
        assertEquals(null, actual);
    }

    @Test
    public void testGetAllocatedTarget_Primordial_JavaLangCharacter() {
        // setup
        setupSiteAndCGNode(TypeReference.JavaLangCharacter);
        // exercise
        IClass allocated = sut.getAllocatedTarget(caller, site);
        // verify
        assertEquals(TypeReference.JavaLangCharacter, allocated.getReference());
    }

    @Test
    public void testGetAllocatedTarget_Interface_JavaLangSet() {
        // setup
        setupSiteAndCGNode(TypeReference.JavaUtilSet);
        // exercise
        IClass allocated = sut.getAllocatedTarget(caller, site);
        // verify
        assertTrue(allocated instanceof BypassSyntheticClass);
        IClass realType = ((BypassSyntheticClass) allocated).getRealType();
        assertEquals(TypeReference.JavaUtilSet, realType.getReference());
    }

    private void setupSiteAndCGNode(TypeReference declaredNewSiteType) {
        site = createNewSiteMock();
        caller = createCGNodeMock();
        cha = JREOnlyClassHierarchyFixture.getInstance();
        mockNewSiteGetDeclaredType(site, declaredNewSiteType);
        mockCGNodeGetClassHierarchy(caller, cha);
    }
}
