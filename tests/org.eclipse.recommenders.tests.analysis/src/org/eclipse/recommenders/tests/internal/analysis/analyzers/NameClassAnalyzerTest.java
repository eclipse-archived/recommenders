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
package org.eclipse.recommenders.tests.internal.analysis.analyzers;

import static org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils.wala2recTypeName;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createClassMock;
import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.internal.analysis.analyzers.NameClassAnalyzer;
import org.eclipse.recommenders.internal.analysis.analyzers.modules.NameClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.junit.Before;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;

public class NameClassAnalyzerTest {
    TypeDeclaration data;

    NameClassAnalyzer sut;

    @Before
    public void beforeTest() {
        data = TypeDeclaration.create();
        sut = new NameClassAnalyzer();
    }

    @Test
    public void testAnalyzeClass() {
        // setup
        final IClass exampleClass = createClassMock("Lsome/Name");
        final ITypeName expected = wala2recTypeName(exampleClass);
        // exercise
        sut.analyzeClass(exampleClass, data, null);
        // verify
        assertEquals(expected, data.name);
    }

    @Test(expected = Exception.class)
    public void testModule() {
        new NameClassAnalyzerPluginModule().configure();
    }
}
