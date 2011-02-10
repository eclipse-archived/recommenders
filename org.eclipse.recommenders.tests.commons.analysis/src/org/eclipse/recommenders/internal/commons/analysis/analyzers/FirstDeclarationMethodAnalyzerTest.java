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

import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.FirstDeclarationMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.junit.Before;
import org.junit.Test;

public class FirstDeclarationMethodAnalyzerTest {
    MethodDeclaration data;

    IMethodAnalyzer sut;

    @Before
    public void beforeTest() {
        data = MethodDeclaration.create();
        sut = new FirstDeclarationMethodAnalyzer();
    }

    @Test
    public void testAnalyzeMethod() {
        //
    }

    @Test(expected = Exception.class)
    public void testModule() {
        new FirstDeclarationMethodAnalyzerPluginModule().configure();
    }
}
