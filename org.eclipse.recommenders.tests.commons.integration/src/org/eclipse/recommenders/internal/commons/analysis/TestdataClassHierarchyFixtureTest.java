package org.eclipse.recommenders.internal.commons.analysis;

import static org.junit.Assert.fail;

import org.eclipse.recommenders.tests.commons.analysis.utils.TestdataClassHierarchyFixture;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

public class TestdataClassHierarchyFixtureTest {

    @Test
    public void testFixture() {
        IClassHierarchy cha = TestdataClassHierarchyFixture.getInstance();
        for (IClass clazz : cha) {
            ClassLoaderReference clRef = clazz.getClassLoader().getReference();
            if (clRef.equals(ClassLoaderReference.Application)) {
                return;
            }
        }
        fail("no classes loaded with application class loader. Classpath is not working properly");
    }
}
