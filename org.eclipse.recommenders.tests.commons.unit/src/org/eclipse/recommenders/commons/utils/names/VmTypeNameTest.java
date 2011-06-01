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
package org.eclipse.recommenders.commons.utils.names;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.recommenders.commons.utils.NamesTest;
import org.junit.Ignore;
import org.junit.Test;

public class VmTypeNameTest {

    @Test
    public void testGetArrayBaseType() {
        final ITypeName actual = NamesTest.LONG_ARRAY_3DIM.getArrayBaseType();
        assertEquals(NamesTest.LONG, actual);
    }

    @Test
    public void testGetArrayDimensions_3dLongArray() {
        final int actual = NamesTest.LONG_ARRAY_3DIM.getArrayDimensions();
        assertEquals(3, actual);
    }

    @Test
    public void testGetArrayDimensions_LongArray() {
        final int actual = NamesTest.LONG_ARRAY.getArrayDimensions();
        assertEquals(1, actual);
    }

    @Test
    public void testDifferentNameObjectsAfterGC() {
        final String key = "Lweak/Value";
        final int hashCode1 = VmTypeName.get(key).hashCode();
        final int hashCode2 = VmTypeName.get(key).hashCode();
        assertTrue(hashCode1 == hashCode2);
        System.gc();
        final int hashCode3 = VmTypeName.get(key).hashCode();
        assertFalse(hashCode1 == hashCode3);
    }

    @Test
    public void testSameNameObjectsAfterGCWithHardReferenceInCode() {
        final String key = "Lweak/Value";
        final VmTypeName expected = VmTypeName.get(key);
        System.gc();
        assertEquals(expected, VmTypeName.get(key));
    }

    @Test
    public void testGetArrayDimensions_Long() {
        final int actual = NamesTest.LONG.getArrayDimensions();
        assertEquals(0, actual);
    }

    @Test
    public void testGetArrayDimensions_String() {
        final int actual = NamesTest.STRING.getArrayDimensions();
        assertEquals(0, actual);
    }

    @Test
    public void testGetClassName_String() {
        final String actual = NamesTest.STRING.getClassName();
        assertEquals("String", actual);
    }

    @Test
    public void testGetClassName_MapEntry() {
        final String actual = NamesTest.MAP$ENTRY.getClassName();
        assertEquals("Map$Entry", actual);
    }

    @Test
    public void testGetClassName_PrimitiveLong() {
        final String actual = NamesTest.LONG.getClassName();
        assertEquals("J", actual);
    }

    @Test
    public void testGetClassName_DefaultPackage() {
        final String className = NamesTest.CLASS_IN_DEFAULT_PACAKGE.getClassName();
        assertEquals("Default", className);
    }

    @Test
    public void testGetPackage_Primitive() {
        final IPackageName actual = NamesTest.LONG.getPackage();
        final IPackageName expected = VmPackageName.get("");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPackage_String() {
        final IPackageName actual = NamesTest.STRING.getPackage();
        final IPackageName expected = VmPackageName.get("java/lang");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPackage_ClassInDefaultPackage() {
        final IPackageName actual = NamesTest.CLASS_IN_DEFAULT_PACAKGE.getPackage();
        final IPackageName expected = VmPackageName.get("");
        assertEquals(expected, actual);
    }

    @Test
    public void testIsAnonymousType_DefaultPackage() {
        final boolean actual = NamesTest.CLASS_IN_DEFAULT_PACAKGE.isAnonymousType();
        assertEquals(false, actual);
    }

    @Test
    public void testIsAnonymousType_Primitive() {
        final boolean actual = NamesTest.LONG.isAnonymousType();
        assertEquals(false, actual);
    }

    @Test
    public void testIsAnonymousType_PrimitiveArray() {
        final boolean actual = NamesTest.LONG_ARRAY.isAnonymousType();
        assertEquals(false, actual);
    }

    @Test
    public void testIsAnonymousType_Map$Entry() {
        final boolean actual = NamesTest.MAP$ENTRY.isAnonymousType();
        assertEquals(false, actual);
    }

    @Test
    public void testIsAnonymousType_Anonymous() {
        final boolean actual = NamesTest.ANONYMOUS.isAnonymousType();
        assertEquals(true, actual);
    }

    @Test
    public void testIsArrayType() {
        assertEquals(true, NamesTest.LONG_ARRAY.isArrayType());
        assertEquals(false, NamesTest.STRING.isArrayType());
    }

    @Test
    public void testIsDeclaredType() {
        assertEquals(true, NamesTest.STRING.isDeclaredType());
        assertEquals(false, NamesTest.LONG.isDeclaredType());
        assertEquals(false, NamesTest.LONG_ARRAY.isDeclaredType());
    }

    @Test
    public void testIsNestedType() {
        assertEquals(true, NamesTest.MAP$ENTRY.isNestedType());
        assertEquals(false, NamesTest.STRING.isNestedType());
        assertEquals(false, NamesTest.LONG_ARRAY.isNestedType());
    }

    @Test
    public void testIsPrimitiveType() {
        assertEquals(false, NamesTest.MAP$ENTRY.isPrimitiveType());
        assertEquals(false, NamesTest.STRING.isPrimitiveType());
        assertEquals(false, NamesTest.LONG_ARRAY.isPrimitiveType());
        assertEquals(true, NamesTest.LONG.isPrimitiveType());
    }

    @Test
    public void testIsVoid() {
        assertEquals(false, NamesTest.MAP$ENTRY.isVoid());
        assertEquals(false, NamesTest.STRING.isVoid());
        assertEquals(false, NamesTest.LONG_ARRAY.isVoid());
        assertEquals(false, NamesTest.LONG.isVoid());
        assertEquals(true, VmTypeName.VOID.isVoid());
    }

    @Test
    public void testCompareTo() {
        assertEquals(0, NamesTest.MAP$ENTRY.compareTo(NamesTest.MAP$ENTRY));
        assertTrue(0 > NamesTest.MAP$ENTRY.compareTo(NamesTest.STRING));
    }

    @Test(expected = IllegalStateException.class)
    @Ignore("does not work in OSGI test suite")
    public void testNewVmTypeName_InvalidPrimitive() {
        new VmTypeName("M");
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore("does not work in OSGI test suite")
    public void testNewVmTypeName_Empty() {
        new VmTypeName("");
    }

    @Test
    @Ignore("does not work in OSGI test suite")
    public void testNewVmTypeName_Nested() {
        final VmTypeName actual = new VmTypeName(NamesTest.MAP$ENTRY.getIdentifier());
        // NOTE: object identity is different. Don't use equals on VmTypeNames!
        assertEquals(NamesTest.MAP$ENTRY.getIdentifier(), actual.getIdentifier());
    }

    @Test
    public void testParsingPackageInfoClassFile() {
        VmTypeName.get("Ltest/package-info");
        // it should not throw an exception (anymore)
    }

    @Test
    public void testGetDeclaringType() {
        assertEquals(NamesTest.MAP, NamesTest.MAP$ENTRY.getDeclaringType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDeclaringType_Exception() {
        NamesTest.MAP.getDeclaringType();
    }

    @Test
    public void testNameWithGenerics() {
        VmTypeName.get("Lm/M<Lpackage/N>");
    }
}
