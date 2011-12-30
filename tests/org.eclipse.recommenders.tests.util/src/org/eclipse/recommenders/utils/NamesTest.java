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
package org.eclipse.recommenders.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.recommenders.utils.Names.PrimitiveType;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmPackageName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

import com.google.common.collect.Lists;

public class NamesTest {
    public static final ITypeName STRING = VmTypeName.get("Ljava/lang/String");

    public static final IPackageName JAVA_UTIL = VmPackageName.get("java/util");

    public static final IPackageName JAVA_LANG = VmPackageName.get("java/lang");

    public static final IPackageName JAVA_UTIL_CONCURRENT = VmPackageName.get("java/util/concurrent");

    public static final ITypeName LONG = VmTypeName.get("J");

    public static final ITypeName LONG_ARRAY = VmTypeName.get("[J");

    public static final ITypeName LONG_ARRAY_3DIM = VmTypeName.get("[[[J");

    public static final ITypeName MAP = VmTypeName.get("Ljava/utils/Map");

    public static final ITypeName MAP$ENTRY = VmTypeName.get("Ljava/utils/Map$Entry");

    public static final ITypeName CLASS_IN_DEFAULT_PACAKGE = VmTypeName.get("LDefault");

    public static final IMethodName STRING_HASHCODE = VmMethodName.get("Ljava/lang/String.hashCode()I");

    public static final IMethodName STRING_NEW = VmMethodName.get("Ljava/lang/String.<init>()V");

    public static final IMethodName STRING_WAIT = VmMethodName.get("Ljava/lang/String.wait(I)V");

    public static final IMethodName STRING_VIRTUAL = VmMethodName
            .get("Ljava/lang/String.virtual(I[[Ljava/lang/String;Ljava/lang/Object;)V");

    public static final ITypeName ANONYMOUS = VmTypeName.get("Ljava/awt/SomeClass$17");

    @Test
    public void testSrc2vmType_Primitives() {
        for (final PrimitiveType primitive : PrimitiveType.values()) {
            final String src = primitive.src();
            final String expected = primitive.vm() + "";
            final String actual = Names.src2vmType(src);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testVm2srcSimpleMethod() {
        assertEquals("wait(int)", Names.vm2srcSimpleMethod(STRING_WAIT));
        assertEquals("hashCode()", Names.vm2srcSimpleMethod(STRING_HASHCODE));
        assertEquals("String()", Names.vm2srcSimpleMethod(STRING_NEW));
    }

    @Test
    public void testVm2srcPackage() {
        assertEquals("java.lang", Names.vm2srcPackage(JAVA_LANG));
        assertEquals("java.util.concurrent", Names.vm2srcPackage(JAVA_UTIL_CONCURRENT));
    }

    @Test
    public void testSrc2vmType_SomeReferenceTypes() {
        assertEquals("Ljava/lang/String", Names.src2vmType("java.lang.String"));
        assertEquals("LString", Names.src2vmType("String"));
    }

    @Test
    public void testVm2srcQualifiedType_WithArrays() {
        assertEquals("long[][][]", Names.vm2srcQualifiedType(LONG_ARRAY_3DIM));
    }

    @Test
    public void testVm2srcQualifiedType_WithPrimitives() {
        assertEquals("int", Names.vm2srcQualifiedType(VmTypeName.INT));
        assertEquals("double", Names.vm2srcQualifiedType(VmTypeName.DOUBLE));
        assertEquals("long", Names.vm2srcQualifiedType(VmTypeName.LONG));
        assertEquals("float", Names.vm2srcQualifiedType(VmTypeName.FLOAT));
        assertEquals("char", Names.vm2srcQualifiedType(VmTypeName.CHAR));
        assertEquals("void", Names.vm2srcQualifiedType(VmTypeName.VOID));
        assertEquals("boolean", Names.vm2srcQualifiedType(VmTypeName.BOOLEAN));
        assertEquals("byte", Names.vm2srcQualifiedType(VmTypeName.BYTE));
        assertEquals("null", Names.vm2srcQualifiedType(VmTypeName.NULL));
        assertEquals("short", Names.vm2srcQualifiedType(VmTypeName.SHORT));
    }

    @Test
    public void testVm2srcQualifiedType_WithReferenceTypes() {
        assertEquals("java.lang.Object", Names.vm2srcQualifiedType(VmTypeName.OBJECT));
    }

    @Test
    public void testVm2SrcSimpleNameFromName() {
        final String expected = "String";
        final String actual = Names.vm2srcSimpleTypeName(STRING);
        assertEquals(expected, actual);
    }

    @Test
    public void testVm2SrcSimpleNameFromNameString_WithPrimitive() {
        assertEquals("int", Names.vm2srcSimpleTypeName("I"));
        assertEquals("double", Names.vm2srcSimpleTypeName("D"));
        assertEquals("long", Names.vm2srcSimpleTypeName("J"));
        assertEquals("float", Names.vm2srcSimpleTypeName("F"));
        assertEquals("char", Names.vm2srcSimpleTypeName("C"));
        assertEquals("void", Names.vm2srcSimpleTypeName("V"));
        assertEquals("boolean", Names.vm2srcSimpleTypeName("Z"));
        assertEquals("byte", Names.vm2srcSimpleTypeName("B"));
        assertEquals("short", Names.vm2srcSimpleTypeName("S"));
    }

    @Test
    public void testVm2SrcSimpleNameFromNameWithPrimitive() {
        final String expected = "long";
        final String actual = Names.vm2srcSimpleTypeName(LONG);
        assertEquals(expected, actual);
    }

    @Test
    public void testVm2SrcSimpleNameFromNameWithPrimitive2DimensionalArray() {
        final String expected = "long[][][]";
        final String actual = Names.vm2srcSimpleTypeName(LONG_ARRAY_3DIM);
        assertEquals(expected, actual);
    }

    @Test
    public void testVm2SrcSimpleNameFromNameWithPrimitiveArray() {
        final String expected = "long[]";
        final String actual = Names.vm2srcSimpleTypeName(LONG_ARRAY);
        assertEquals(expected, actual);
    }

    @Test
    public void testVm2SrcTypeNameFromString() {
        final String expected = "java.lang.String";
        final String value = STRING.getIdentifier();
        final String actual = Names.vm2srcTypeName(value);
        assertEquals(expected, actual);
    }

    @Test
    public void testParseMethodSignature1() {
        final String value = STRING_VIRTUAL.getIdentifier();
        final String[] actual = Names.parseMethodSignature1(value);
        assertEquals(5, actual.length);
        assertEquals("Ljava/lang/String.virtual", actual[0]);
        assertEquals("int", actual[1]);
        assertEquals("java.lang.String[][]", actual[2]);
        assertEquals("java.lang.Object", actual[3]);
        assertEquals("void", actual[actual.length - 1]);
    }

    @Test
    public void testParseMethodSignature2() {
        final String value = STRING_WAIT.getIdentifier();
        final String[] actual = Names.parseMethodSignature2(value);
        assertEquals(2, actual.length);
        assertEquals("Ljava/lang/String", actual[0]);
        assertEquals("wait(I)V", actual[1]);
    }

    @Test
    public void testParseMethodSignature3() {
        final String value = STRING_WAIT.getIdentifier();
        final String[] actual = Names.parseMethodSignature3(value);
        assertEquals(3, actual.length);
        assertEquals("Ljava/lang/String", actual[0]);
        assertEquals("wait", actual[1]);
        assertEquals("(I)V", actual[2]);
    }

    @Test
    public void testSrc2vmMethod() {
        final String expected = STRING_WAIT.getIdentifier();
        final String actual = Names.src2vmMethod("Ljava/lang/String.wait", new String[] { "int" }, "void");
        assertEquals(expected, actual);
    }

    @Test
    public void testSrc2vmType() {
        // "int[][][]" not supported
        final String[] srcTypes = new String[] { "int", "void", "java.lang.String" };
        final List<String> expecteds = Lists.newArrayList("I", "V", "Ljava/lang/String");
        final List<String> actuals = Names.src2vmType(srcTypes);
        assertEquals(expecteds, actuals);
    }

    @Test
    public void testVm2srcQualifiedMethod() {
        final String expected = "java.lang.String.virtual(int, String[][], Object)";
        final String actual = Names.vm2srcQualifiedMethod(STRING_VIRTUAL);
        assertEquals(expected, actual);
    }
}
