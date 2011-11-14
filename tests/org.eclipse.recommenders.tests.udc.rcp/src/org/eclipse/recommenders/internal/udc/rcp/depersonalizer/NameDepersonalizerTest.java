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
package org.eclipse.recommenders.internal.udc.rcp.depersonalizer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeReference;
import org.eclipse.recommenders.internal.udc.depersonalizer.NameDepersonalizer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NameDepersonalizerTest {
    static CompilationUnitProvider unitProvider;
    CompilationUnit unit;

    @BeforeClass
    public static void createProvider() {
        unitProvider = new CompilationUnitProvider();

    }

    @Before
    public void prepareUnit() {
        createUnit();
        depersonalize();
    }

    private void createUnit() {
        unit = unitProvider.createCompilationUnit();
    }

    private void depersonalize() {
        final NameDepersonalizer depersonalizer = new NameDepersonalizer();
        depersonalizer.depersonalize(unit);
    }

    @Test
    public void imports() {
        assertTrue(unit.imports.size() > 0);
        for (final TypeReference ref : unit.imports) {
            assertNull(ref.name);
        }
    }

    @Test
    public void unitName() {
        assertNull(unit.name);
    }

    @Test
    public void primaryTypeName() {
        assertNull(unit.primaryType.name);
    }

    @Test
    public void fields() {
        assertNull(unit.primaryType.fields);
    }

    @Test
    public void methodNames() {
        createUnit();
        final MethodDeclaration trySendDataMethod = unit.findMethod(unitProvider
                .getMethodName(CompilationUnitProvider.TRY_SEND_DATA_METHOD));
        final MethodDeclaration exportUnitsMethod = unit.findMethod(unitProvider
                .getMethodName(CompilationUnitProvider.EXPORT_UNITS_METHOD));
        depersonalize();
        assertNull(trySendDataMethod.name);
        assertNull(exportUnitsMethod.name);
    }

    @Test
    public void definitionSite() {
        createUnit();
        final MethodDeclaration trySendDataMethod = unit.findMethod(unitProvider
                .getMethodName(CompilationUnitProvider.TRY_SEND_DATA_METHOD));
        final ObjectInstanceKey object = unitProvider.getObject(trySendDataMethod,
                CompilationUnitProvider.WSCLIENT_OBJECT);
        depersonalize();
        assertNull(object.definitionSite);
    }

    @Test
    public void parameterCallSites() {
        createUnit();
        final MethodDeclaration trySendDataMethod = unit.findMethod(unitProvider
                .getMethodName(CompilationUnitProvider.TRY_SEND_DATA_METHOD));
        final ObjectInstanceKey object = unitProvider
                .getObject(trySendDataMethod, CompilationUnitProvider.UNITS_OBJECT);
        depersonalize();
        for (final ParameterCallSite site : object.parameterCallSites) {
            assertNull(site.argumentName);
            assertNull(site.sourceMethod);
            assertNull(site.targetMethod);
        }
    }

    @Test
    public void receiverCallSites() {
        createUnit();
        final MethodDeclaration trySendDataMethod = unit.findMethod(unitProvider
                .getMethodName(CompilationUnitProvider.TRY_SEND_DATA_METHOD));
        final ObjectInstanceKey object = unitProvider.getObject(trySendDataMethod,
                CompilationUnitProvider.WSCLIENT_OBJECT);
        depersonalize();
        for (final ReceiverCallSite site : object.receiverCallSites) {
            assertNull(site.receiver);
            assertNull(site.sourceMethod);
        }
    }

}
