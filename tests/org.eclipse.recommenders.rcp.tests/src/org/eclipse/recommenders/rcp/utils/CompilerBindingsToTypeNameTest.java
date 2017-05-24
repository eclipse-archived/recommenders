/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - Initial API and implementation
 */
package org.eclipse.recommenders.rcp.utils;

import static org.eclipse.recommenders.testing.CodeBuilder.*;
import static org.eclipse.recommenders.utils.names.VmTypeName.get;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@SuppressWarnings("restriction")
public class CompilerBindingsToTypeNameTest {

    private final CharSequence code;
    private final ITypeName expected;

    public CompilerBindingsToTypeNameTest(String description, CharSequence code, ITypeName expected) {
        this.code = code;
        this.expected = expected;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = new LinkedList<>();

        // Real-world scenarios
        scenarios.add(scenario("Primitive type", method("$int i = 0;"), get("I")));
        scenarios.add(scenario("Reference type", method("$Object o = null;"), get("Ljava/lang/Object")));
        scenarios.add(scenario("Array of primitives", method("$int[] is = null;"), get("[I")));
        scenarios.add(scenario("Array of objects", method("$Object[] os = null;"), get("[Ljava/lang/Object")));
        scenarios.add(scenario("Raw generic type", method("$Collection c = 0;"), get("Ljava/util/Collection")));
        scenarios.add(scenario("Void type", classbody("$void method() {}"), get("V")));

        // Synthetic scenarios
        scenarios.add(scenario("Type is type parameter of declaring class", classbody("Example<T>", "$T t = null;"),
                get("Ljava/lang/Object")));
        scenarios.add(scenario("Type is upper-bounded type parameter of declaring class",
                classbody("Example<N extends Number>", "$N n = null"), get("Ljava/lang/Number")));
        scenarios.add(scenario("Type is upper-bounded type parameter of declaring class (superfluous bound of Object)",
                classbody("Example<O extends Object>", "$O o = null;"), get("Ljava/lang/Object")));
        scenarios.add(scenario("Method argument is upper-bounded type parameter of declaring class (multiple bounds)",
                classbody("Example<N extends Number & Comparable>", "$N n = null;"), get("Ljava/lang/Number")));
        scenarios.add(scenario(
                "Type is upper-bounded type parameter of declaring class (bound is parameterized itself)",
                classbody("Example<L extends List<String>>", "$L l = null;"), get("Ljava/util/List")));

        return scenarios;
    }

    private static Object[] scenario(String description, CharSequence code, ITypeName expected) {
        return new Object[] { description, code, expected };
    }

    @Test
    public void test() throws Exception {
        TypeBinding binding = getBinding(code);

        ITypeName actual = CompilerBindings.toTypeName(binding).get();

        assertThat(actual, is(equalTo(expected)));
    }

    private static TypeBinding getBinding(CharSequence code) throws Exception {
        return getBinding(CompilerBindingsTestUtils.getCompilerAstNode(code));
    }

    private static TypeBinding getBinding(ASTNode node) {
        if (node instanceof SingleTypeReference) {
            return ((SingleTypeReference) node).resolvedType;
        } else {
            throw new IllegalArgumentException(node.toString());
        }
    }
}
