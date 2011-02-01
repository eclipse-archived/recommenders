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
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import static org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils.wala2recTypeName;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createClassMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockClassGetSuperclass;
import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ExtendsClauseClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.junit.Before;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;

public class DeclaredSuperclassClassAnalyzerTest {
    TypeDeclaration data;

    DeclaredSuperclassClassAnalyzer sut;

    @Before
    public void beforeTest() {
        data = TypeDeclaration.create();
        sut = new DeclaredSuperclassClassAnalyzer();
    }

    @Test
    public void testAnalyzeClass() {
        // setup
        final IClass baseclass = createClassMock();
        final IClass superclass = createClassMock("Lsome/Name");
        final ITypeName superClassName = wala2recTypeName(superclass);
        mockClassGetSuperclass(baseclass, superclass);
        // exercise
        sut.analyzeClass(baseclass, data);
        // verify
        assertEquals(superClassName, data.superclass);
    }

    @Test(expected = Exception.class)
    public void testModule() {
        new ExtendsClauseClassAnalyzerPluginModule().configure();
    }
}
