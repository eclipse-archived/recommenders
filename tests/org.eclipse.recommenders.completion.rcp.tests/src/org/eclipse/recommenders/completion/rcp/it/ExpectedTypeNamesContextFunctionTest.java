package org.eclipse.recommenders.completion.rcp.it;

import static org.eclipse.recommenders.testing.CodeBuilder.*;
import static org.eclipse.recommenders.utils.names.VmTypeName.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture;
import org.eclipse.recommenders.testing.rcp.jdt.JavaContentAssistContextMock;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ExpectedTypeNamesContextFunctionTest {

    private static ITypeName OBJECT_ARRAY = VmTypeName.get("[Ljava/lang/Object");
    private static ITypeName STRING = VmTypeName.get("Ljava/lang/String");
    private static ITypeName STRING_ARRAY = VmTypeName.get("[Ljava/lang/String");
    private static ITypeName FILE = VmTypeName.get("Ljava/io/File");
    private static ITypeName COLLECTION = VmTypeName.get("Ljava/util/Collection");
    private static ITypeName URI = VmTypeName.get("Ljava/net/URI");

    private final CharSequence code;
    private final ITypeName[] expectedTypes;

    public ExpectedTypeNamesContextFunctionTest(CharSequence code, ITypeName[] expectedTypes) {
        this.code = code;
        this.expectedTypes = expectedTypes;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(method("new File($);"), FILE, STRING, URI));
        scenarios.add(scenario(method("File f = $;"), FILE));
        scenarios.add(scenario(classbody("File method() { return $; }"), FILE));

        scenarios.add(scenario(method("List<String> l = new ArrayList<String>($);"), COLLECTION, INT));
        scenarios.add(scenario(method("List<String> l = new ArrayList<String>(); l.add($)"), STRING, INT));
        // fails
        // scenarios.add(scenario(method("List<String> l = new ArrayList<String>(); l.toArray($)"), STRING_ARRAY));

        scenarios.add(scenario(method("Arrays.asList($);"), OBJECT_ARRAY));

        scenarios.add(scenario(method("if ($) {}"), BOOLEAN));
        scenarios.add(scenario(method("while ($) {}"), BOOLEAN));

        scenarios.add(scenario(classbody("void method() { } void caller() { method($); }")));

        scenarios.add(scenario(classbody("void method(int i) { } void caller() { method($); }"), INT));
        scenarios.add(scenario(classbody("void method(String s) { } void caller() { method($); }"), STRING));

        scenarios.add(scenario(classbody("<T> void method(T t) { } void caller() { method($); }"), OBJECT));
        // fails
        // scenarios.add(scenario(classbody("<N extends Number> void method(N n) { } void caller() { method($); }"),
        // get("Ljava/lang/Number")));

        scenarios.add(
                scenario(classbody("<T> void method(Collection<?> c) { } void caller() { method($); }"), COLLECTION));
        scenarios.add(
                scenario(classbody("<T> void method(Collection<T> c) { } void caller() { method($); }"), COLLECTION));
        scenarios.add(scenario(classbody("<T> void method(Collection<? extends T> c) { } void caller() { method($); }"),
                COLLECTION));
        scenarios.add(scenario(classbody("<T> void method(Collection<? super T> c) { } void caller() { method($); }"),
                COLLECTION));

        return scenarios;
    }

    <T extends Number> void m(T t) {
    }

    private static Object[] scenario(CharSequence code, ITypeName... expectedTypes) {
        return new Object[] { code, expectedTypes };
    }

    @Test
    public void test() throws Exception {
        IRecommendersCompletionContext sut = createCompletionContext(code);

        Set<ITypeName> result = sut.getExpectedTypeNames();

        assertThat(result, CoreMatchers.hasItems(expectedTypes));
        assertThat(result.size(), is(equalTo(expectedTypes.length)));
    }

    private IRecommendersCompletionContext createCompletionContext(CharSequence code) throws CoreException {
        JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");
        Pair<ICompilationUnit, Set<Integer>> struct = fixture.createFileAndParseWithMarkers(code.toString());
        ICompilationUnit cu = struct.getFirst();
        int completionIndex = struct.getSecond().iterator().next();
        JavaContentAssistInvocationContext javaContext = new JavaContentAssistContextMock(cu, completionIndex);
        return new RecommendersCompletionContext(javaContext, new CachingAstProvider());
    }
}
