package org.eclipse.recommenders.completion.rcp.utils;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.eclipse.recommenders.testing.CodeBuilder.classbody;
import static org.eclipse.recommenders.utils.names.VmMethodName.get;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture;
import org.eclipse.recommenders.testing.rcp.jdt.JavaContentAssistContextMock;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class ProposalUtilsTest {

    private static IMethodName METHOD_VOID = VmMethodName.get("LExample.method()V");
    private static IMethodName METHOD_OBJECT = VmMethodName.get("LExample.method(Ljava/lang/Object;)V");
    private static IMethodName METHOD_NUMBER = VmMethodName.get("LExample.method(Ljava/lang/Number;)V");
    private static IMethodName METHOD_COLLECTION = VmMethodName.get("LExample.method(Ljava/util/Collection;)V");
    private static IMethodName SET_INT_STRING = VmMethodName
            .get("Ljava/util/List.set(ILjava/lang/Object;)Ljava/lang/Object;");

    private static IMethodName NESTED_METHOD_VOID = VmMethodName.get("LExample$Nested.method()V");

    private static IMethodName METHOD_INTS = VmMethodName.get("LExample.method([I)V");
    private static IMethodName METHOD_OBJECTS = VmMethodName.get("LExample.method([Ljava/lang/Object;)V");

    private static IMethodName INIT = VmMethodName.get("LExample.<init>()V");
    private static IMethodName INIT_OBJECT = VmMethodName.get("LExample.<init>(Ljava/lang/Object;)V");
    private static IMethodName INIT_NUMBER = VmMethodName.get("LExample.<init>(Ljava/lang/Number;)V");

    private static IMethodName NESTED_INIT = VmMethodName.get("LExample$Nested.<init>()V");
    private static IMethodName NESTED_INIT_OBJECT = VmMethodName.get("LExample$Nested.<init>(Ljava/lang/Object;)V");
    private static IMethodName NESTED_INIT_NUMBER = VmMethodName.get("LExample$Nested.<init>(Ljava/lang/Number;)V");

    private static IMethodName COMPARE_TO_BOOLEAN = VmMethodName
            .get("Ljava/lang/Boolean.compareTo(Ljava/lang/Boolean;)I");
    private static IMethodName COMPARE_TO_OBJECT = VmMethodName
            .get("Ljava/lang/Comparable.compareTo(Ljava/lang/Object;)I");

    private final CharSequence code;
    private final IMethodName expectedMethod;

    public ProposalUtilsTest(CharSequence code, IMethodName expectedMethod) {
        this.code = code;
        this.expectedMethod = expectedMethod;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(classbody("Example", "void method() { this.method$ }"), METHOD_VOID));
        scenarios.add(scenario(classbody("Example", "void method(Object o) { this.method$ }"), METHOD_OBJECT));
        scenarios.add(scenario(classbody("Example", "void method(Collection c) { this.method$ }"), METHOD_COLLECTION));

        scenarios.add(scenario(classbody("Example", "void method(int[] is) { this.method$ }"), METHOD_INTS));
        scenarios.add(scenario(classbody("Example", "void method(Object[] os) { this.method$ }"), METHOD_OBJECTS));

        scenarios.add(scenario(classbody("Example", "static class Nested { void method() { this.method$ } }"),
                NESTED_METHOD_VOID));
        scenarios.add(scenario(classbody("Example<T>", "static class Nested { void method() { this.method$ } }"),
                NESTED_METHOD_VOID));
        scenarios.add(scenario(classbody("Example", "static class Nested<T> { void method() { this.method$ } }"),
                NESTED_METHOD_VOID));

        scenarios.add(scenario(classbody("Example", "void method(Collection<Number> c) { this.method$ }"),
                METHOD_COLLECTION));
        scenarios
                .add(scenario(classbody("Example", "void method(Collection<?> c) { this.method$ }"), METHOD_COLLECTION));
        scenarios.add(scenario(classbody("Example", "void method(Collection<? extends Number> c) { this.method$ }"),
                METHOD_COLLECTION));
        scenarios.add(scenario(classbody("Example", "void method(Collection<? super Number> c) { this.method$ }"),
                METHOD_COLLECTION));

        scenarios.add(scenario(classbody("Example<T>", "void method(T t) { this.method$ }"), METHOD_OBJECT));
        scenarios.add(scenario(classbody("Example<O extends Object>", "void method(O o) { this.method$ }"),
                METHOD_OBJECT));
        scenarios.add(scenario(classbody("Example<N extends Number>", "void method(N n) { this.method$ }"),
                METHOD_NUMBER));
        scenarios
                .add(scenario(classbody("Example<N extends Number & Comparable>", "void method(N n) { this.method$ }"),
                        METHOD_NUMBER));

        scenarios.add(scenario(classbody("Example<L extends List<String>>", "void method(L l) { l.set$ }"),
                SET_INT_STRING));

        String auxiliaryDefinition = "class Auxiliary<L extends List<String>> { <N extends L> void method(N n) { } }";
        scenarios.add(scenario(classbody("Example", "void method(Auxiliary a) { a.method$ }") + auxiliaryDefinition,
                get("LAuxiliary.method(Ljava/util/List;)V")));

        scenarios.add(scenario(classbody("Example<T>", "void method(T[] t) { this.method$ }"), METHOD_OBJECTS));
        scenarios.add(scenario(classbody("Example<O extends Object>", "void method(O[] o) { this.method$ }"),
                METHOD_OBJECTS));

        scenarios.add(scenario(classbody("Example<N extends Number>", "void method(Collection<N> c) { this.method$ }"),
                METHOD_COLLECTION));

        scenarios.add(scenario(classbody("Example", "<T> void method(T t) { this.method$ }"), METHOD_OBJECT));
        scenarios.add(scenario(classbody("Example", "<O extends Object> void method(O o) { this.method$ }"),
                METHOD_OBJECT));
        scenarios.add(scenario(classbody("Example", "<N extends Number> void method(N n) { this.method$ }"),
                METHOD_NUMBER));
        scenarios.add(scenario(
                classbody("Example", "<N extends Number & Comparable> void method(N n) { this.method$ }"),
                METHOD_NUMBER));

        scenarios.add(scenario(classbody("Example", "<T> void method(T[] t) { this.method$ }"), METHOD_OBJECTS));
        scenarios.add(scenario(classbody("Example", "<O extends Object> void method(O[] o) { this.method$ }"),
                METHOD_OBJECTS));

        scenarios.add(scenario(classbody("Example", "void method(Boolean b) { b.compareTo$ }"), COMPARE_TO_BOOLEAN));
        scenarios.add(scenario(classbody("Example", "void method(Delayed d) { d.compareTo$ }"), COMPARE_TO_OBJECT));

        scenarios.add(scenario(classbody("Example", "Example() { this($) }"), INIT));
        scenarios.add(scenario(classbody("Example<T>", "Example(T t) { this($) }"), INIT_OBJECT));
        scenarios.add(scenario(classbody("Example<T extends Object>", "Example(T t) { this($) }"), INIT_OBJECT));
        scenarios.add(scenario(classbody("Example<N extends Number>", "Example(N n) { this($) }"), INIT_NUMBER));

        // Using nested classes to speed up JDT's constructor completion; this avoids timeouts.
        scenarios.add(scenario(classbody("Example", "static class Nested { Nested() { new Example.Nested$ } }"),
                NESTED_INIT));
        scenarios.add(scenario(classbody("Example", "static class Nested<T> { Nested(T t) { new Example.Nested$ } }"),
                NESTED_INIT_OBJECT));
        scenarios.add(scenario(
                classbody("Example", "static class Nested<T extends Object> { Nested(T t) { new Example.Nested$ } }"),
                NESTED_INIT_OBJECT));
        scenarios.add(scenario(
                classbody("Example", "static class Nested<N extends Number> { Nested(N n) { new Example.Nested$ } }"),
                NESTED_INIT_NUMBER));

        scenarios.add(scenario(classbody("Example implements Comparable", "compareTo$"), COMPARE_TO_OBJECT));
        scenarios.add(scenario(classbody("Example implements Comparable<Example>", "compareTo$"), COMPARE_TO_OBJECT));
        scenarios.add(scenario(classbody("Example<T> implements Comparable<T>", "compareTo$"), COMPARE_TO_OBJECT));
        scenarios.add(scenario(classbody("Example<N extends Number> implements Comparable<N>", "compareTo$"),
                COMPARE_TO_OBJECT));

        return scenarios;
    }

    private static Object[] scenario(CharSequence compilationUnit, IMethodName expectedMethod) {
        return new Object[] { compilationUnit, expectedMethod };
    }

    @Test
    public void test() throws Exception {
        IRecommendersCompletionContext context = extractProposals(code);
        Collection<CompletionProposal> proposals = context.getProposals().values();
        Optional<LookupEnvironment> environment = context.get(CompletionContextKey.LOOKUP_ENVIRONMENT);

        IMethodName actualMethod = ProposalUtils.toMethodName(getOnlyElement(proposals), environment.orNull()).get();

        assertThat(actualMethod, is(equalTo(expectedMethod)));
    }

    private IRecommendersCompletionContext extractProposals(CharSequence code) throws CoreException {
        JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");
        Pair<ICompilationUnit, Set<Integer>> struct = fixture.createFileAndParseWithMarkers(code.toString());
        ICompilationUnit cu = struct.getFirst();
        int completionIndex = struct.getSecond().iterator().next();
        JavaContentAssistInvocationContext javaContext = new JavaContentAssistContextMock(cu, completionIndex);
        IRecommendersCompletionContext recommendersContext = new RecommendersCompletionContext(javaContext,
                new CachingAstProvider());
        return recommendersContext;
    }
}
