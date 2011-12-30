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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeReference;
import org.eclipse.recommenders.internal.udc.depersonalizer.ObjectUsageFilter;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjectUsageFilterTest {
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

    private void depersonalize(final String... allowedTypes) {
        final Set<String> allowedLibraries = new HashSet<String>();
        for (final String type : allowedTypes) {
            allowedLibraries.add(unitProvider.typeName2FingerpintMapping.get(type));
        }
        final ObjectUsageFilter filter = new ObjectUsageFilter(allowedLibraries);
        filter.depersonalize(unit);
    }

    @Test
    public void testImports() {
        depersonalize(CompilationUnitProvider.LIST);
        final Set<TypeReference> expectedLibraries = new HashSet<TypeReference>();
        expectedLibraries.add(unitProvider.getImport(CompilationUnitProvider.LIST));
        expectedLibraries.add(unitProvider.getImport(CompilationUnitProvider.OBJECT));
        assertEquals(expectedLibraries, unit.imports);
    }

    @Test
    public void testSuperClassRemoved() {
        depersonalize(CompilationUnitProvider.IPROJECT);
        assertNull(unit.primaryType.superclass);
    }

    @Test
    public void testSuperClassRemains() {
        depersonalize(CompilationUnitProvider.OBJECT);
        assertEquals(unit.primaryType.superclass.getIdentifier(), CompilationUnitProvider.OBJECT);
    }

    @Test
    public void testInterfacesRemoved() {
        depersonalize(CompilationUnitProvider.IPROJECT);
        assertTrue(unit.primaryType.interfaces.isEmpty());
    }

    @Test
    public void testInterfaceRemains() {
        depersonalize(CompilationUnitProvider.IPROGRESS_MONITOR);
        final ITypeName typeName = VmTypeName.get(CompilationUnitProvider.IPROGRESS_MONITOR);
        final Set<ITypeName> expectedInterfaces = new HashSet<ITypeName>();
        expectedInterfaces.add(typeName);
        assertEquals(expectedInterfaces, unit.primaryType.interfaces);
    }

    @Test
    public void testFieldRemains() {
        depersonalize(CompilationUnitProvider.IPROGRESS_MONITOR);
        final ITypeName typeName = VmTypeName.get(CompilationUnitProvider.IPROGRESS_MONITOR);
        final Set<ITypeName> expectedFields = new HashSet<ITypeName>();
        expectedFields.add(typeName);
        assertEquals(expectedFields, unit.primaryType.fields);
    }

    @Test
    public void testFieldRemoved() {
        depersonalize(CompilationUnitProvider.IPROJECT);
        assertTrue(unit.primaryType.fields.isEmpty());
    }

    @Test
    public void testObjectUsageInstances() {
        depersonalize(CompilationUnitProvider.OBJECT);
        final MethodDeclaration method = unit.findMethod(unitProvider
                .getMethodName(CompilationUnitProvider.TRY_SEND_DATA_METHOD));
        assertEquals(2, method.objects.size());
        final ObjectInstanceKey units_object = unitProvider.getObject(method, CompilationUnitProvider.UNITS_OBJECT);
        assertEquals(new HashSet<ParameterCallSite>(), units_object.parameterCallSites);

        final ObjectInstanceKey thisObject = unitProvider.getObject(method, CompilationUnitProvider.THIS_OBJECT);
    }
}
