/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.depersonalizer;

import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnitVisitor;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.udc.depersonalizer.LineNumberDepersonalizer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LineNumberDepersonalizerTest {
    static CompilationUnitProvider unitProvider;
    CompilationUnit unit;

    @BeforeClass
    public static void createProvider() {
        unitProvider = new CompilationUnitProvider();
    }

    @Before
    public void createUnit() {
        unit = unitProvider.createCompilationUnit();
    }

    @Test
    public void testLineNumbers() {
        final LineNumberDepersonalizer depersonalizer = new LineNumberDepersonalizer();
        depersonalizer.depersonalize(unit);
        testRemoved(unit.primaryType.line);
        unit.accept(new CompilationUnitVisitor() {
            @Override
            public boolean visit(final MethodDeclaration method) {
                testRemoved(method.line);
                return super.visit(method);
            }

            @Override
            public boolean visit(final ParameterCallSite parameterCallSite) {
                testRemoved(parameterCallSite.lineNumber);
                return super.visit(parameterCallSite);
            }

            @Override
            public boolean visit(final ReceiverCallSite receiverCallSite) {
                testRemoved(receiverCallSite.line);
                return super.visit(receiverCallSite);
            }

            @Override
            public boolean visit(final ObjectInstanceKey objectInstanceKey) {
                final DefinitionSite site = objectInstanceKey.definitionSite;
                if (site != null) {
                    testRemoved(site.lineNumber);
                }
                return super.visit(objectInstanceKey);
            }

            @Override
            public boolean visit(final TypeDeclaration type) {
                testRemoved(type.line);
                return super.visit(type);
            }
        });
    }

    private void testRemoved(final int lineNumber) {
        assertEquals(-1, lineNumber);
    }
}
