/**
 * Copyright (c) 2010, 2011, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sebastian Proksch - initial API and implementation
 *    Kevin Munk - Extension for more tests for extracting package names
 */
package org.eclipse.recommenders.testing.jdt;

import static org.eclipse.recommenders.testing.jdt.JavaProjectFixture.*;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class JavaProjectFixtureTest {

    private StringBuilder code;

    @Before
    public void setup() {
        code = new StringBuilder();
        code.append("public class Class1 {");
        code.append("     public static class Inner {");
        code.append("             public static final int i = 9;");
        code.append("         }");
        code.append("     @Action // possible indicator for non __test named entry points (javax.xml.ws.Action;)");
        code.append("     public int hashCode() {");
        code.append("         Inner i = new Inner() {};");
        code.append("         Object obj = new Object();");
        code.append("         return obj.hashCode();");
        code.append("     }");
        code.append("     @Action");
        code.append("     public void XYZ() {");
        code.append("         // nothing");
        code.append("     }");
        code.append("}");
    }

    @Test
    public void extractNameFromClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class Class1 {");
        sb.append("     public void __test() {");
        sb.append("    }");
        sb.append("}");

        String actual = findClassName(sb);
        String expected = "Class1";

        assertEquals(expected, actual);
    }

    @Test
    public void extractNameFromClassWithoutVisibilityModifier() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Class1 {");
        sb.append("     void __test() {");
        sb.append("    }");
        sb.append("}");

        String actual = findClassName(sb);
        String expected = "Class1";

        assertEquals(expected, actual);
    }

    @Test
    public void extractNameFromInterface() {
        StringBuilder sb = new StringBuilder();
        sb.append("public interface Interface1 {");
        sb.append("     public void __test();");
        sb.append("}");

        String actual = findClassName(sb);
        String expected = "Interface1";

        assertEquals(expected, actual);
    }

    @Test
    public void extractNameWithInnerClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class Class1 {");
        sb.append("    public static class InnerTest {");
        sb.append("        public void doit() {}");
        sb.append("        public static void doitStatic() {}");
        sb.append("     }");
        sb.append("     public void __test() {");
        sb.append("    }");
        sb.append("}");

        String actual = findClassName(sb);
        String expected = "Class1";

        assertEquals(expected, actual);
    }

    @Test
    public void extractNameWithInnerInterface() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class Class1 {");
        sb.append("    public static interface InnerFace {");
        sb.append("        public void doit();");
        sb.append("     }");
        sb.append("     public void __test() {");
        sb.append("    }");
        sb.append("}");

        String actual = findClassName(sb);
        String expected = "Class1";

        assertEquals(expected, actual);
    }

    @Test
    public void extractNamesOfInnerClasses() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class Class1 {");
        sb.append("    public static class Inner1 {}");
        sb.append("    public static class Inner2 {}");
        sb.append("}");

        List<String> actuals = JavaProjectFixture.findInnerClassNames(sb);
        List<String> expecteds = Lists.newArrayList("Class1$Inner1", "Class1$Inner2");

        assertEquals(expecteds, actuals);
    }

    @Test
    public void extractNamesOfAnonymousClasses() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class Class1 {");
        sb.append("    public void blubb() {");
        sb.append("         Object o = new Object() {");
        sb.append("             if(true) {");
        sb.append("                 // bla");
        sb.append("             } else {");
        sb.append("                 // blubb");
        sb.append("             }");
        sb.append("         }");
        sb.append("         Object o = new Object() {};");
        sb.append("    }");
        sb.append("}");

        List<String> actuals = findAnonymousClassNames(sb);
        List<String> expecteds = Lists.newArrayList("Class1$1", "Class1$2");

        assertEquals(expecteds, actuals);
    }

    @Test
    public void extractInnerClassesFromBiggerExample() {
        List<String> actuals = findInnerClassNames(code);
        List<String> expecteds = Lists.newArrayList("Class1$Inner");
        assertEquals(expecteds, actuals);
    }

    @Test
    public void extractAnonymousClassesFromBiggerExample() {
        List<String> actuals = findAnonymousClassNames(code);
        List<String> expecteds = Lists.newArrayList("Class1$1");
        assertEquals(expecteds, actuals);
    }

    @Test
    public void extractNoPackageName() {
        String actual = findPackageName(code);
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void extractNoPackageNameFaultyPackage1() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void extractNoPackageNameFaultyPackage2() {
        StringBuilder sb = new StringBuilder();
        sb.append("package test.");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void extractNoPackageNameFaultyPackage3() {
        StringBuilder sb = new StringBuilder();
        sb.append("package test.blub.");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void extractNoPackageNameFaultyPackage4() {
        StringBuilder sb = new StringBuilder();
        sb.append("package test.;");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void extractNoPackageNameFaultyPackage5() {
        StringBuilder sb = new StringBuilder();
        sb.append("package test.blub.;");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void extractPackageName() {
        StringBuilder sb = new StringBuilder();
        sb.append("package test;");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "test";
        assertEquals(expected, actual);
    }

    @Test
    public void extractPackageName2() {
        StringBuilder sb = new StringBuilder();
        sb.append("@SomeAnnotation");
        sb.append("package test.blub.bla;");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "test.blub.bla";
        assertEquals(expected, actual);
    }

    @Test
    public void extractPackageName3() {
        StringBuilder sb = new StringBuilder();
        sb.append("package a.b.c.d.e.f;");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "a.b.c.d.e.f";
        assertEquals(expected, actual);
    }

    @Test
    public void extractPackageNameUnicode() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ñ.b.c.d.ê.f;");
        sb.append("public class Class1 {");
        sb.append("}");

        String actual = findPackageName(sb);
        String expected = "ñ.b.c.d.ê.f";
        assertEquals(expected, actual);
    }
}
