package org.eclipse.recommenders.calls.rcp;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.eclipse.recommenders.testing.CodeBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.calls.rcp.ProposalMatcher;
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

@RunWith(Parameterized.class)
public class ProposalMatcherTest {

    private static IMethodName METHOD_VOID = VmMethodName.get("Lorg/example/Any.method()V");
    private static IMethodName METHOD_OBJECT = VmMethodName.get("Lorg/example/Any.method(Ljava/lang/Object;)V");
    private static IMethodName METHOD_NUMBER = VmMethodName.get("Lorg/example/Any.method(Ljava/lang/Number;)V");
    private static IMethodName METHOD_COLLECTION = VmMethodName.get("Lorg/example/Any.method(Ljava/util/Collection;)V");

    private static IMethodName METHOD_INTS = VmMethodName.get("Lorg/example/Any.method([I)V");
    private static IMethodName METHOD_OBJECTS = VmMethodName.get("Lorg/example/Any.method([Ljava/lang/Object;)V");

    private final CharSequence code;
    private final IMethodName method;
    private final boolean matchExpected;

    public ProposalMatcherTest(CharSequence code, IMethodName method, boolean matchExpected) {
        this.code = code;
        this.method = method;
        this.matchExpected = matchExpected;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(mismatch(classbody("void methodWithDifferentName() { this.method$ }"), METHOD_VOID));
        scenarios.add(mismatch(classbody("void method(int i) { this.method$ }"), METHOD_VOID));
        scenarios.add(mismatch(classbody("void method(int i) { this.method$ }"), METHOD_OBJECT));

        scenarios.add(match(classbody("void method() { this.method$ }"), METHOD_VOID));
        scenarios.add(match(classbody("void method(Object o) { this.method$ }"), METHOD_OBJECT));
        scenarios.add(match(classbody("void method(Collection c) { this.method$ }"), METHOD_COLLECTION));

        scenarios.add(match(classbody("void method(int[] is) { this.method$ }"), METHOD_INTS));
        scenarios.add(match(classbody("void method(Object[] os) { this.method$ }"), METHOD_OBJECTS));

        scenarios.add(match(classbody("void method(Collection<Number> c) { this.method$ }"), METHOD_COLLECTION));
        scenarios.add(match(classbody("void method(Collection<?> c) { this.method$ }"), METHOD_COLLECTION));
        scenarios.add(match(classbody("void method(Collection<? extends Number> c) { this.method$ }"),
                METHOD_COLLECTION));
        scenarios
        .add(match(classbody("void method(Collection<? super Number> c) { this.method$ }"), METHOD_COLLECTION));

        scenarios.add(match(classbody(classname() + "<T>", "void method(T t) { this.method$ }"), METHOD_OBJECT));
        scenarios.add(match(classbody(classname() + "<O extends Object>", "void method(O o) { this.method$ }"),
                METHOD_OBJECT));
        scenarios.add(match(classbody(classname() + "<N extends Number>", "void method(N n) { this.method$ }"),
                METHOD_NUMBER));
        scenarios.add(match(
                classbody(classname() + "<N extends Number & Comparable>", "void method(N n) { this.method$ }"),
                METHOD_NUMBER));

        scenarios.add(match(classbody(classname() + "<T>", "void method(T[] t) { this.method$ }"), METHOD_OBJECTS));
        scenarios.add(match(classbody(classname() + "<O extends Object>", "void method(O[] o) { this.method$ }"),
                METHOD_OBJECTS));

        scenarios.add(match(
                classbody(classname() + "<N extends Number>", "void method(Collection<N> c) { this.method$ }"),
                METHOD_COLLECTION));

        scenarios.add(match(classbody("<T> void method(T t) { this.method$ }"), METHOD_OBJECT));
        scenarios.add(match(classbody("<O extends Object> void method(O o) { this.method$ }"), METHOD_OBJECT));
        scenarios.add(match(classbody("<N extends Number> void method(N n) { this.method$ }"), METHOD_NUMBER));
        scenarios.add(match(classbody("<N extends Number & Comparable> void method(N n) { this.method$ }"),
                METHOD_NUMBER));

        scenarios.add(match(classbody("<T> void method(T[] t) { this.method$ }"), METHOD_OBJECTS));
        scenarios.add(match(classbody("<O extends Object> void method(O[] o) { this.method$ }"), METHOD_OBJECTS));

        return scenarios;
    }

    private static Object[] match(CharSequence compilationUnit, IMethodName method) {
        return new Object[] { compilationUnit, method, true };
    }

    private static Object[] mismatch(CharSequence compilationUnit, IMethodName method) {
        return new Object[] { compilationUnit, method, false };
    }

    @Test
    public void testMatch() throws Exception {
        IRecommendersCompletionContext context = extractProposals(code);
        Collection<CompletionProposal> proposals = context.getProposals().values();
        Optional<TypeBinding> receiverTypeBinding = context.get(CompletionContextKey.RECEIVER_TYPEBINDING);
        ProposalMatcher sut = new ProposalMatcher(getOnlyElement(proposals), receiverTypeBinding);

        assertThat(sut.match(method), is(equalTo(matchExpected)));
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
