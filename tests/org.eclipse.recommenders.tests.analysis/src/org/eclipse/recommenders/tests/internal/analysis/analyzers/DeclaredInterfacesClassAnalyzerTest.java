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

import static org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils.wala2recTypeNames;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createClassMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createInterface;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.internal.analysis.analyzers.DeclaredInterfacesClassAnalyzer;
import org.eclipse.recommenders.internal.analysis.analyzers.modules.ImplementsClauseClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;

public class DeclaredInterfacesClassAnalyzerTest {
    TypeDeclaration data;

    DeclaredInterfacesClassAnalyzer sut;

    @Before
    public void beforeTest() {
        data = TypeDeclaration.create();
        sut = new DeclaredInterfacesClassAnalyzer();
    }

    @Test
    public void testAnalyzeClass() {
        // setup
        final IClass c = createClassMock();
        final List<IClass> declaredInterfaces = Lists
                .newArrayList(createInterface("LSome1"), createInterface("LSome1"));
        when(c.getDirectInterfaces()).thenReturn(declaredInterfaces);
        final Set<ITypeName> expecteds = Sets.newHashSet(wala2recTypeNames(declaredInterfaces));
        // exercise
        sut.analyzeClass(c, data, null);
        // verify
        assertEquals(expecteds, data.interfaces);
    }

    @Test(expected = Exception.class)
    public void testModule() {
        new ImplementsClauseClassAnalyzerPluginModule().configure();
    }
}
