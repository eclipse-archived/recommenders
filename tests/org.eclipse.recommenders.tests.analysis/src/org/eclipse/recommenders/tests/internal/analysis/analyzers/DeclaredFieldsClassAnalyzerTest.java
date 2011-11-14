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

import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createClassMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPrivateIntegerField;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPublicStringField;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.mockClassGetDeclaredFields;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.internal.analysis.analyzers.DeclaredFieldsClassAnalyzer;
import org.eclipse.recommenders.internal.analysis.analyzers.modules.DeclaredFieldsClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;

;
public class DeclaredFieldsClassAnalyzerTest {
    TypeDeclaration data;

    DeclaredFieldsClassAnalyzer sut;

    @Before
    public void beforeTest() {
        data = TypeDeclaration.create();
        sut = new DeclaredFieldsClassAnalyzer();
    }

    @Test
    public void testAnalyzeClass() {
        // setup
        final List<IField> declaredFields = Lists.newArrayList(createPublicStringField(), createPrivateIntegerField());
        final IClass exampleClass = createClassMock();
        mockClassGetDeclaredFields(exampleClass, declaredFields);
        final Set<ITypeName> expecteds = Sets.newHashSet(WalaNameUtils.walaFields2recTypeNames(declaredFields));
        // exercise
        sut.analyzeClass(exampleClass, data, null);
        // verify
        assertEquals(expecteds, data.fields);
    }

    @Test(expected = Exception.class)
    public void testModule() {
        new DeclaredFieldsClassAnalyzerPluginModule().configure();
    }
}
