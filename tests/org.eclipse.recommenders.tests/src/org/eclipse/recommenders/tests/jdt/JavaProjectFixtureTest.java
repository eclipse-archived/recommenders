/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.tests.jdt;

import static org.eclipse.recommenders.tests.jdt.JavaProjectFixture.findClassName;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class JavaProjectFixtureTest {

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

        List<String> actuals = JavaProjectFixture.findAnonymousClassNames(sb);
        List<String> expecteds = Lists.newArrayList("Class1$1", "Class1$2");

        assertEquals(expecteds, actuals);
    }
}