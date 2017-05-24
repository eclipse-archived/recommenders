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
import static org.eclipse.recommenders.utils.names.VmMethodName.get;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@SuppressWarnings("restriction")
public class CompilerBindingsToMethodNameTest {

    private final CharSequence code;
    private final IMethodName expected;

    public CompilerBindingsToMethodNameTest(String description, CharSequence code, IMethodName expected) {
        this.code = code;
        this.expected = expected;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = new LinkedList<>();

        // Real-world scenarios
        scenarios.add(scenario("Method without arguments, void return type", method("$wait();"),
                get("Ljava/lang/Object.wait()V")));
        scenarios.add(scenario("Primitive argument, void return type", method("$wait(0L);"),
                get("Ljava/lang/Object.wait(J)V")));
        scenarios.add(scenario("Method with reference argument, void return type",
                method("System.out.$println(\"\");"), get("Ljava/io/PrintStream.println(Ljava/lang/String;)V")));

        scenarios.add(scenario("Method without arguments, primitive return type", method("$hashCode();"),
                get("Ljava/lang/Object.hashCode()I")));
        scenarios.add(scenario("Method withoutarguments, reference return type", method("$toString();"),
                get("Ljava/lang/Object.toString()Ljava/lang/String;")));

        scenarios.add(scenario("Constructor without arguments", method("$new String();"),
                get("Ljava/lang/String.<init>()V")));

        // Synthetic scenarios
        scenarios.add(scenario("Method argument is array of primitives",
                classbody("Example", "void method(int[] is) { $method(null); }"), get("LExample.method([I)V")));
        scenarios.add(scenario("Method argument is array of objects",
                classbody("Example", "void method(Object[] os) { $method(null); }"),
                get("LExample.method([Ljava/lang/Object;)V")));
        scenarios.add(scenario("Method argument is array of arrays of objects",
                classbody("Example", "void method(Object[][] os) { $method(null); }"),
                get("LExample.method([[Ljava/lang/Object;)V")));

        scenarios.add(scenario("Method argument is parameterized type (invariant)",
                classbody("Example", "void method(Collection<Number> c) { $method(null); }"),
                get("LExample.method(Ljava/util/Collection;)V")));
        scenarios.add(scenario("Method argument is parameterized type (wildcard)",
                classbody("Example", "void method(Collection<?> c) { $method(null); }"),
                get("LExample.method(Ljava/util/Collection;)V")));
        scenarios.add(scenario("Method argument is parameterized type (upper-bounded wildcard)",
                classbody("Example", "void method(Collection<? extends Number> c) { $method(null); }"),
                get("LExample.method(Ljava/util/Collection;)V")));
        scenarios.add(scenario("Method argument is parameterized type (lower-bounded wildcard)",
                classbody("Example", "void method(Collection<? super Number> c) { $method(null); }"),
                get("LExample.method(Ljava/util/Collection;)V")));

        scenarios.add(scenario("Method argument is type parameter of declaring class",
                classbody("Example<T>", "void method(T t) { $method(null); }"),
                get("LExample.method(Ljava/lang/Object;)V")));
        scenarios.add(scenario("Method argument is upper-bounded type parameter of declaring class",
                classbody("Example<N extends Number>", "void method(N n) { $method(null); }"),
                get("LExample.method(Ljava/lang/Number;)V")));
        scenarios.add(scenario(
                "Method argument is upper-bounded type parameter of declaring class (superfluous bound of Object)",
                classbody("Example<O extends Object>", "void method(O o) { $method(null); }"),
                get("LExample.method(Ljava/lang/Object;)V")));
        scenarios.add(scenario("Method argument is upper-bounded type parameter of declaring class (multiple bounds)",
                classbody("Example<N extends Number & Comparable>", "void method(N n) { $method(null); }"),
                get("LExample.method(Ljava/lang/Number;)V")));
        scenarios.add(scenario(
                "Method argument is upper-bounded type parameter of declaring class (bound is parameterized itself)",
                classbody("Example<L extends List<String>>", "void method(L l) { $method(null); }"),
                get("LExample.method(Ljava/util/List;)V")));

        scenarios.add(scenario("Method argument is type parameter of method",
                classbody("Example", "<T> void method(T t) { $method(null); }"),
                get("LExample.method(Ljava/lang/Object;)V")));
        scenarios.add(scenario("Method argument is upper-bounded type parameter of method",
                classbody("Example", "<N extends Number> void method(N n) { $method(null); }"),
                get("LExample.method(Ljava/lang/Number;)V")));
        scenarios.add(scenario(
                "Method argument is upper-bounded type parameter of method (superfluous bound of Object)",
                classbody("Example", "<O extends Object> void method(O o) { $method(null); }"),
                get("LExample.method(Ljava/lang/Object;)V")));
        scenarios.add(scenario("Method argument is upper-bounded type parameter of method (multiple bounds)",
                classbody("Example", "<N extends Number & Comparable> void method(N n) { $method(null); }"),
                get("LExample.method(Ljava/lang/Number;)V")));
        scenarios.add(scenario(
                "Method argument is upper-bounded type parameter of method (bound is parameterized itself)",
                classbody("Example", "<L extends List<String>> void method(L l) { $method(null); }"),
                get("LExample.method(Ljava/util/List;)V")));

        scenarios.add(scenario("Method argument is type parameter of implemented interface",
                classbody("Example implements Comparable<Number>", "int compareTo(Number n) { $compareTo(null); }"),
                get("LExample.compareTo(Ljava/lang/Number;)I")));

        scenarios.add(scenario("Constructor argument is type parameter of declaring class (raw)",
                classbody("Example<T>", "Example(T t) { $new Example(null); }"),
                get("LExample.<init>(Ljava/lang/Object;)V")));
        scenarios.add(scenario("Constructor argument is type parameter of declaring class",
                classbody("Example<T>", "Example(T t) { $new Example<Number>(null); }"),
                get("LExample.<init>(Ljava/lang/Object;)V")));
        scenarios.add(scenario("Constructor argument is upper-bounded type parameter of declaring class",
                classbody("Example<N extends Number>", "Example(N n) { $new Example<Integer>(null); }"),
                get("LExample.<init>(Ljava/lang/Number;)V")));

        scenarios.add(scenario("Constructor argument is parameterized by type parameter of declaring class",
                classbody("Example<T>", "Example(Collection<T> ts) { $new Example<Integer>(null); }"),
                get("LExample.<init>(Ljava/util/Collection;)V")));

        return scenarios;
    }

    private static Object[] scenario(String description, CharSequence code, IMethodName expected) {
        return new Object[] { description, code, expected };
    }

    @Test
    public void test() throws Exception {
        MethodBinding binding = getBinding(code);

        IMethodName actual = CompilerBindings.toMethodName(binding).get();

        assertThat(actual, is(equalTo(expected)));
    }

    private static MethodBinding getBinding(CharSequence code) throws Exception {
        return getBinding(CompilerBindingsTestUtils.getCompilerAstNode(code));
    }

    private static MethodBinding getBinding(ASTNode node) {
        if (node instanceof MessageSend) {
            return ((MessageSend) node).binding;
        } else if (node instanceof AllocationExpression) {
            return ((AllocationExpression) node).binding;
        } else {
            throw new IllegalArgumentException(node.toString());
        }
    }
}
