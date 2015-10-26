package org.eclipse.recommenders.completion.rcp.it;

import static org.eclipse.recommenders.testing.CodeBuilder.classDeclaration;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.testing.rcp.completion.rules.TemporaryWorkspace;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Test that generic types and their (first) bound are taken into account when completion is triggered on a generic
 * return value.
 *
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=404751">Bug 404751</a>
 */
@RunWith(Parameterized.class)
public class Bug404751Test {

    @ClassRule
    public static final TemporaryWorkspace WORKSPACE = new TemporaryWorkspace();

    private final boolean ignore;
    private final String expectedType;
    private final String typeParameters;
    private final String typeArguments;

    public Bug404751Test(boolean ignore, String expectedType, String typeParameters, String typeArguments) {
        this.ignore = ignore;
        this.expectedType = expectedType;
        this.typeParameters = typeParameters;
        this.typeArguments = typeArguments;
    }

    @Parameters(name = "{index}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("Object", null, null));
        scenarios.add(scenario("Number", null, "Number"));

        scenarios.add(scenario("Number", "Number", null));
        scenarios.add(scenario("Integer", "Number", "Integer"));

        scenarios.add(scenario("List", "List", null));
        scenarios.add(scenario("List", "List<?>", null));
        scenarios.add(scenario("List", "List<?>", "List"));
        scenarios.add(scenario("ArrayList", "List<?>", "ArrayList"));
        scenarios.add(scenario("List", "List<?>", "List<?>"));
        scenarios.add(scenario("ArrayList", "List<?>", "ArrayList<?>"));
        scenarios.add(scenario("List", "List<T>", null));

        // See <https://bugs.eclipse.org/bugs/show_bug.cgi?id=461562>
        scenarios.add(ignoredScenario("List", "List & Closeable", null));
        scenarios.add(ignoredScenario("Closeable", "Closeable & List", null));
        scenarios.add(ignoredScenario("Number", "Number & List", null));
        scenarios.add(ignoredScenario("Number", "Number & List & Closeable", null));

        return scenarios;
    }

    private static Object[] scenario(String expectedType, String tExtends, String t) {
        return scenario(false, expectedType, tExtends, t);
    }

    private static Object[] ignoredScenario(String expectedType, String tExtends, String t) {
        return scenario(true, expectedType, tExtends, t);
    }

    private static Object[] scenario(boolean ignore, String expectedType, String tExtends, String t) {
        String typeParameters = tExtends == null ? "<T>" : "<T extends " + tExtends + ">";
        String typeArguments = t == null ? "" : "<" + t + ">";
        return new Object[] { ignore, expectedType, typeParameters, typeArguments };
    }

    @Test
    public void testReceiverTypeOfInstanceMethod() throws Exception {
        assumeThat(ignore, is(equalTo(false)));

        String producerMethod = "T produce() { return null; }";
        String consumerMethod = "static void consume() { new TestClass" + typeArguments + "().produce().$; }";
        CharSequence code = classDeclaration("class TestClass" + typeParameters, producerMethod + consumerMethod);

        IRecommendersCompletionContext sut = WORKSPACE.createProject().createFile(code).triggerContentAssist();
        IType receiverType = sut.getReceiverType().get();

        assertThat(receiverType.getElementName(), is(equalTo(expectedType)));
    }

    @Test
    public void testReceiverTypeOfStaticMethod() throws Exception {
        assumeThat(ignore, is(equalTo(false)));

        String producerMethod = "static " + typeParameters + " T produce() { return null; }";
        String consumerMethod = "static void consume() { TestClass." + typeArguments + "produce().$; }";
        CharSequence code = classDeclaration("class TestClass", producerMethod + consumerMethod);

        IRecommendersCompletionContext sut = WORKSPACE.createProject().createFile(code).triggerContentAssist();
        IType receiverType = sut.getReceiverType().get();

        assertThat(receiverType.getElementName(), is(equalTo(expectedType)));
    }
}
