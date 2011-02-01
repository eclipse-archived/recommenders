package org.eclipse.recommenders.internal.commons.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisFixture;
import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisScopeBuilder;
import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultAnalysisScopeBuilder;
import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultClassHierarchyBuilder;
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
        int LEAST_NUMBER_OF_JDK_CLASSES = 1000;
        IAnalysisFixture fixture = JREOnlyAnalysisFixture.create();
        IAnalysisScopeBuilder scopeBuilder = DefaultAnalysisScopeBuilder.buildFromFixture(fixture);
        AnalysisScope scope = scopeBuilder.getAnalysisScope();
        DefaultClassHierarchyBuilder sut = new DefaultClassHierarchyBuilder(scope);
        //
        // exercise
        IClassHierarchy cha = sut.getClassHierachy();
        System.out.println("number of classes: " + cha.getNumberOfClasses());
        //
        // verify
        assertTrue(cha.getNumberOfClasses() > LEAST_NUMBER_OF_JDK_CLASSES);
        for (IClass clazz : cha) {
            assertEquals(clazz.getClassLoader().getReference(), ClassLoaderReference.Primordial);
        }
    }
}
