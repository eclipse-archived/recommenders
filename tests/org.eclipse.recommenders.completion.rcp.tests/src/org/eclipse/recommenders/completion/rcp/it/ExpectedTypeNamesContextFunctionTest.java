package org.eclipse.recommenders.completion.rcp.it;

import static org.eclipse.recommenders.testing.CodeBuilder.*;
import static org.eclipse.recommenders.utils.names.VmTypeName.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.testing.rcp.completion.rules.TemporaryWorkspace;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.hamcrest.CoreMatchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ExpectedTypeNamesContextFunctionTest {

    @ClassRule
    public static final TemporaryWorkspace WORKSPACE = new TemporaryWorkspace();

    private static final ITypeName OBJECT_ARRAY = VmTypeName.get("[Ljava/lang/Object");
    private static final ITypeName STRING = VmTypeName.get("Ljava/lang/String");
    private static final ITypeName FILE = VmTypeName.get("Ljava/io/File");
    private static final ITypeName COLLECTION = VmTypeName.get("Ljava/util/Collection");
    private static final ITypeName URI = VmTypeName.get("Ljava/net/URI");

    private final CharSequence code;
    private final ITypeName[] expectedTypes;

    public ExpectedTypeNamesContextFunctionTest(CharSequence code, ITypeName[] expectedTypes) {
        this.code = code;
        this.expectedTypes = expectedTypes;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = new LinkedList<>();

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

        scenarios.add(scenario(classbody("<T> void method(Collection<?> c) { } void caller() { method($); }"),
                COLLECTION));
        scenarios.add(scenario(classbody("<T> void method(Collection<T> c) { } void caller() { method($); }"),
                COLLECTION));
        scenarios.add(scenario(
                classbody("<T> void method(Collection<? extends T> c) { } void caller() { method($); }"), COLLECTION));
        scenarios.add(scenario(classbody("<T> void method(Collection<? super T> c) { } void caller() { method($); }"),
                COLLECTION));

        return scenarios;
    }

    private static Object[] scenario(CharSequence code, ITypeName... expectedTypes) {
        return new Object[] { code, expectedTypes };
    }

    @Test
    public void test() throws Exception {
        IRecommendersCompletionContext sut = WORKSPACE.createProject().createFile(code).triggerContentAssist();

        Set<ITypeName> result = sut.getExpectedTypeNames();

        assertThat(result, CoreMatchers.hasItems(expectedTypes));
        assertThat(result.size(), is(equalTo(expectedTypes.length)));
    }
}
