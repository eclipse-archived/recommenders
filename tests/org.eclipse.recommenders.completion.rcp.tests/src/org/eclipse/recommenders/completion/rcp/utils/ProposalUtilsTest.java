package org.eclipse.recommenders.completion.rcp.utils;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.eclipse.recommenders.testing.CodeBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.testing.rcp.completion.rules.TemporaryProject;
import org.eclipse.recommenders.testing.rcp.completion.rules.TemporaryWorkspace;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ProposalUtilsTest {

    @ClassRule
    public static final TemporaryWorkspace WORKSPACE = new TemporaryWorkspace();

    private static final IMethodName METHOD_VOID = VmMethodName.get("LExample.method()V");
    private static final IMethodName METHOD_OBJECT = VmMethodName.get("LExample.method(Ljava/lang/Object;)V");
    private static final IMethodName METHOD_NUMBER = VmMethodName.get("LExample.method(Ljava/lang/Number;)V");
    private static final IMethodName METHOD_COLLECTION = VmMethodName.get("LExample.method(Ljava/util/Collection;)V");
    private static final IMethodName SET_INT_STRING = VmMethodName
            .get("Ljava/util/List.set(ILjava/lang/Object;)Ljava/lang/Object;");

    private static final IMethodName NESTED_METHOD_VOID = VmMethodName.get("LExample$Nested.method()V");
    private static final IMethodName INNER_METHOD_VOID = VmMethodName.get("LExample$Inner.method()V");
    private static final IMethodName ANONYMOUS_METHOD_VOID = VmMethodName.get("LScenario$1.method()V");

    private static final IMethodName METHOD_INTS = VmMethodName.get("LExample.method([I)V");
    private static final IMethodName METHOD_OBJECTS = VmMethodName.get("LExample.method([Ljava/lang/Object;)V");

    private static final IMethodName INIT = VmMethodName.get("LExample.<init>()V");
    private static final IMethodName INIT_OBJECT = VmMethodName.get("LExample.<init>(Ljava/lang/Object;)V");
    private static final IMethodName INIT_NUMBER = VmMethodName.get("LExample.<init>(Ljava/lang/Number;)V");

    private static final IMethodName NESTED_INIT = VmMethodName.get("LExample$Nested.<init>()V");
    private static final IMethodName NESTED_INIT_OBJECT = VmMethodName
            .get("LExample$Nested.<init>(Ljava/lang/Object;)V");

    private static final IMethodName INNER_INIT_EXAMPLE = VmMethodName.get("LExample$Inner.<init>(LExample;)V");
    private static final IMethodName INNER_INIT_EXAMPLE_OBJECT = VmMethodName
            .get("LExample$Inner.<init>(LExample;Ljava/lang/Object;)V");

    private static final IMethodName COMPARE_TO_BOOLEAN = VmMethodName
            .get("Ljava/lang/Boolean.compareTo(Ljava/lang/Boolean;)I");
    private static final IMethodName COMPARABLE_COMPARE_TO_OBJECT = VmMethodName
            .get("Ljava/lang/Comparable.compareTo(Ljava/lang/Object;)I");
    private static final IMethodName COMPARE_TO_OBJECT = VmMethodName.get("LExample.compareTo(Ljava/lang/Object;)I");
    private static final IMethodName COMPARE_TO_EXAMPLE = VmMethodName.get("LExample.compareTo(LExample;)I");

    private static final IMethodName OBJECT_HASH_CODE = VmMethodName.get("Ljava/lang/Object.hashCode()I");
    private static final IMethodName EXAMPLE_HASH_CODE = VmMethodName.get("LExample.hashCode()I");
    private static final IMethodName SUBEXAMPLE_HASH_CODE = VmMethodName.get("LSubExample.hashCode()I");

    private static final IMethodName EXAMPLE_CLONE = VmMethodName.get("LExample.clone()Ljava/lang/Object;");
    private static final IMethodName OBJECT_CLONE = VmMethodName.get("Ljava/lang/Object.clone()Ljava/lang/Object;");

    private final boolean ignore;
    private final CharSequence targetTypeCode;
    private final CharSequence completionScenarioCode;
    private final IMethodName expectedMethod;

    public ProposalUtilsTest(boolean ignore, String description, CharSequence targetTypeCode,
            CharSequence completionScenarioCode, IMethodName expectedMethod) {
        this.ignore = ignore;
        this.targetTypeCode = targetTypeCode;
        this.completionScenarioCode = completionScenarioCode;
        this.expectedMethod = expectedMethod;
    }

    @Parameters(name = "{index}: {1}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        // @formatter:off
        scenarios.add(scenario("Method with no parameters",
                classbody("Example", "public void method() {}"),
                method("new Example().method$"),
                METHOD_VOID));
        scenarios.add(scenario("Method with parameter: Object",
                classbody("Example", "public void method(Object o) {}"),
                method("new Example().method$"),
                METHOD_OBJECT));
        scenarios.add(scenario("Method with parameter: int[]",
                classbody("Example", "public void method(int[] is) {}"),
                method("new Example().method$"),
                METHOD_INTS));
        scenarios.add(scenario("Method with parameter: Object[]",
                classbody("Example", "public void method(Object[] os) {}"),
                method("new Example().method$"),
                METHOD_OBJECTS));
        scenarios.add(scenario("Method with parameter: Collection",
                classbody("Example", "public void method(Collection c) {}"),
                method("new Example().method$"),
                METHOD_COLLECTION));
        scenarios.add(scenario("Method with parameter: Collection<Number>",
                classbody("Example", "public void method(Collection<Number> c) {}"),
                method("new Example().method$"),
                METHOD_COLLECTION));
        scenarios.add(scenario("Method with parameter: Collection<?>",
                classbody("Example", "public void method(Collection<?> c) {}"),
                method("new Example().method$"),
                METHOD_COLLECTION));
        scenarios.add(scenario("Method with parameter: Collection<? extends Number>",
                classbody("Example", "public void method(Collection<? extends Number> c) {}"),
                method("new Example().method$"),
                METHOD_COLLECTION));
        scenarios.add(scenario("Method with parameter: Collection<? super Number>",
                classbody("Example", "public void method(Collection<? super Number> c) {}"),
                method("new Example().method$"),
                METHOD_COLLECTION));

        scenarios.add(scenario("Method of nested class",
                classbody("Example", "public static class Nested { public void method() {} }"),
                method("new Example.Nested().method$"),
                NESTED_METHOD_VOID));
        scenarios.add(scenario("Method of parameterized nested class",
                classbody("Example", "public static class Nested<T> { public void method() {} }"),
                method("new Example.Nested<String>().method$"),
                NESTED_METHOD_VOID));
        scenarios.add(scenario("Method of nested class within raw outer class",
                classbody("Example<T>", "public static class Nested { public void method() {} }"),
                method("new Example.Nested().method$"),
                NESTED_METHOD_VOID));

        scenarios.add(scenario("Method of inner class",
                classbody("Example", "public class Inner { public void method() {} }"),
                method("new Example().new Inner().method$"),
                INNER_METHOD_VOID));
        scenarios.add(scenario("Method of parameterized inner class",
                classbody("Example", "public class Inner<T> { public void method() {} }"),
                method("new Example().new Inner<String>().method$"),
                INNER_METHOD_VOID));
        scenarios.add(scenario("Method of inner class within raw outer class",
                classbody("Example<T>", "public class Inner { public void method() {} }"),
                method("new Example().new Inner().method$"),
                INNER_METHOD_VOID));
        scenarios.add(scenario("Method of inner class within parameterized outer class",
                classbody("Example<T>", "public class Inner { public void method() {} }"),
                method("new Example<String>().new Inner().method$"),
                INNER_METHOD_VOID));

        scenarios.add(postJdt451Scenario("Method of anonymous class",
                classbody("Example", "public void method() {}"),
                method("Scenario", "new Example() { public void method() { this.method$ } };"),
                METHOD_VOID));
        scenarios.add(postJdt451Scenario("Method of parameterized anonymous class",
                classbody("Example<T>", "public void method() {}"),
                method("Scenario", "new Example<String>() { public void method() { this.method$ } };"),
                METHOD_VOID));

        scenarios.add(scenario("Generic method with parameter of raw class",
                classbody("Example<T>", "public void method(T t) {}"),
                method("new Example().method$"),
                METHOD_OBJECT));
        scenarios.add(scenario("Generic method with parameter of parameterized class",
                classbody("Example<T>", "public void method(T t) {}"),
                method("new Example<Number>().method$"),
                METHOD_OBJECT));
        scenarios.add(scenario("Method With Unspecified Object Bounded Class Parameter As Argument",
                classbody("Example<O extends Object>", "public void method(O o) {}"),
                method("new Example().method$"),
                METHOD_OBJECT));
        scenarios.add(postJdt451Scenario("Method With Unspecified Bounded Class Parameter As Argument",
                classbody("Example<N extends Number>", "public void method(N n) {}"),
                method("new Example().method$"),
                METHOD_NUMBER));
        scenarios.add(postJdt451Scenario("Method With Specified Bounded Class Parameter As Argument",
                classbody("Example<N extends Number>", "public void method(N n) {}"),
                method("new Example<Integer>().method$"),
                METHOD_NUMBER));
        scenarios.add(postJdt451Scenario("Method With Unspecified Multiple Bound Class Parameter As Argument",
                classbody("Example<N extends Number & Comparable>", "public void method(N n) {}"),
                method("new Example().method$"),
                METHOD_NUMBER));

        scenarios.add(scenario("Generic method throwing exception parameterized on class",
                classbody("Example<T extends Throwable>", "public void method() throws T {}"),
                method("new Example().method$"),
                METHOD_VOID));
        scenarios.add(scenario("Generic method throwing exception parameterized on method",
                classbody("Example", "public <T extends Throwable> void method() throws T {}"),
                method("new Example().method$"),
                METHOD_VOID));

        scenarios.add(scenario("Method Call On Unspecified Bounded Class Parameter With Nested Parameterization",
                classbody("Example<L extends List<String>>", "public L l;"),
                method("new Example().l.set$"),
                SET_INT_STRING));

        String auxiliaryDefinition = "class Auxiliary<L extends List<String>> { public <N extends L> void method(N n) { } }";
        scenarios.add(postJdt451Scenario("Secondary Class With Nested, Bounded Parameters And Method With Bounded Parameter",
                classbody("Example", "void method(Auxiliary a) {}") + auxiliaryDefinition,
                classbody("SubExample extends Example", "void method(Auxiliary a) { a.method$ }"),
                VmMethodName.get("LAuxiliary.method(Ljava/util/List;)V")));

        scenarios.add(scenario("Method With Class Parameter Array As Argument",
                classbody("Example<T>", "public void method(T[] t) {}"),
                method("new Example().method$"),
                METHOD_OBJECTS));
        scenarios.add(scenario("Method With Unspecified Object Bounded Class Parameter Array As Argument",
                classbody("Example<O extends Object>", "public void method(O[] o) {}"),
                method("new Example().method$"),
                METHOD_OBJECTS));
        scenarios.add(scenario("Method With Unspecified Bounded Class Parameter Collection As Argument",
                classbody("Example<N extends Number>", "public void method(Collection<N> c) {}"),
                method("new Example().method$"),
                METHOD_COLLECTION));

        scenarios.add(scenario("Generic method with parameter: T",
                classbody("Example", "public <T> void method(T t) {}"),
                method("new Example().method$"),
                METHOD_OBJECT));
        scenarios.add(scenario("Generic method with parameter: O extends Object",
                classbody("Example", "public <O extends Object> void method(O o) {}"),
                method("new Example().method$"),
                METHOD_OBJECT));
        scenarios.add(postJdt451Scenario("Generic method with parameter: N extends Number",
                classbody("Example", "public <N extends Number> void method(N n) {}"),
                method("new Example().method$"),
                METHOD_NUMBER));
        scenarios.add(postJdt451Scenario("Generic method with parameter: N extends Number & Comparable",
                classbody("Example", "public <N extends Number & Comparable> void method(N n) {}"),
                method("new Example().method$"),
                METHOD_NUMBER));
        scenarios.add(scenario("Generic method with parameter: T[]",
                classbody("Example", "public <T> void method(T[] t) {}"),
                method("new Example().method$"),
                METHOD_OBJECTS));
        scenarios.add(scenario("Generic method with parameter: O[] (O extends Object)",
                classbody("Example", "public <O extends Object> void method(O[] o) {}"),
                method("new Example().method$"),
                METHOD_OBJECTS));

        scenarios.add(scenario("Parameterized static method with parameter: T",
                classbody("Example", "public static <T> void method(T t) {}"),
                method("Example.<Integer>method$"),
                METHOD_OBJECT));
        scenarios.add(scenario("Parameterized static method with parameter: O extends Object",
                classbody("Example", "public static <O extends Object> void method(O o) {}"),
                method("Example.<Integer>method$"),
                METHOD_OBJECT));
        scenarios.add(postJdt451Scenario("Parameterized static method with parameter: N extends Number",
                classbody("Example", "public static <N extends Number> void method(N n) {}"),
                method("Example.<Integer>method$"),
                METHOD_NUMBER));
        scenarios.add(postJdt451Scenario("Parameterized static method with parameter: N extends Number & Comparable",
                classbody("Example", "public static <N extends Number & Comparable> void method(N n) {}"),
                method("Example.<Integer>method$"),
                METHOD_NUMBER));

        scenarios.add(scenario("Method Call On Object Field Of Class",
                classbody("Example", "public Boolean b;"),
                method("new Example().b.compareTo$"),
                COMPARE_TO_BOOLEAN));
        scenarios.add(scenario("Method Call On Interface Field Of Class",
                classbody("Example", "public Delayed d;"),
                method("new Example().d.compareTo$"),
                COMPARABLE_COMPARE_TO_OBJECT));

        scenarios.add(scenario("Implicit constructor",
                classbody("Example", ""),
                classbody("SubExample extends Example", "SubExample() { super($) }"),
                INIT));
        scenarios.add(scenario("Constructor with no parameters",
                classbody("Example", "protected Example() {}"),
                classbody("SubExample extends Example", "SubExample() { super($) }"),
                INIT));
        scenarios.add(scenario("Constructor with parameter: T",
                classbody("Example<T>", "protected Example(T t) {}"),
                classbody("SubExample extends Example", "SubExample() { super($) }"),
                INIT_OBJECT));
        scenarios.add(scenario("Constructor with parameter: O extends Object",
                classbody("Example<O extends Object>", "protected Example(O o) {}"),
                classbody("SubExample extends Example", "SubExample() { super($) }"),
                INIT_OBJECT));
        scenarios.add(postJdt451Scenario("Constructor with parameter: N extends Number",
                classbody("Example<N extends Number>", "protected Example(N n) {}"),
                classbody("SubExample extends Example", "SubExample() { super($) }"),
                INIT_NUMBER));
        scenarios.add(postJdt451Scenario("Constructor with parameter: N extends Number & Comparable",
                classbody("Example<N extends Number>", "protected Example(N n) {}"),
                classbody("SubExample extends Example", "SubExample() { super($) }"),
                INIT_NUMBER));

        scenarios.add(scenario("Constructor of nested class",
                classbody("Example", "public static class Nested { public Nested() {} }"),
                method("new Example.Nested$"),
                NESTED_INIT));
        scenarios.add(scenario("Constructor of generic nested class",
                classbody("Example", "public static class Nested<T> { public Nested(T t) {} }"),
                method("new Example.Nested$"),
                NESTED_INIT_OBJECT));
        scenarios.add(scenario("Constructor of nested class within raw outer class",
                classbody("Example<T>", "public static class Nested { public Nested() {} }"),
                method("new Example.Nested$"),
                NESTED_INIT));

        scenarios.add(postJdt451Scenario("Constructor of inner class",
                classbody("Example", "public class Inner { public Inner() {} }"),
                method("new Example().new Inner$"),
                INNER_INIT_EXAMPLE));
        scenarios.add(postJdt451Scenario("Constructor of generic inner class",
                classbody("Example", "public class Inner<T> { public Inner(T t) {} }"),
                method("new Example().new Inner$"),
                INNER_INIT_EXAMPLE_OBJECT));
        scenarios.add(postJdt451Scenario("Constructor of inner class within raw outer class",
                classbody("Example<T>", "public class Inner { public Inner(T t) {} }"),
                method("new Example().new Inner$"),
                INNER_INIT_EXAMPLE_OBJECT));
        scenarios.add(postJdt451Scenario("Constructor of inner class within parameterized outer class",
                classbody("Example<T>", "public class Inner { public Inner(T t) {} }"),
                method("new Example<String>().new Inner$"),
                INNER_INIT_EXAMPLE_OBJECT));

        scenarios.add(scenario("Override Superclass Method Inherited From Interface",
                classbody("Example implements Comparable", "public int compareTo(Object o) {return 0;} "),
                classbody("SubExample extends Example", "compareTo$"),
                COMPARE_TO_OBJECT));
        scenarios.add(scenario("Override Superclass Method Inherited From Interface, Interface Parameterized By Superclass",
                classbody("Example implements Comparable<Example>", "public int compareTo(Example example) {return 0;} "),
                classbody("SubExample extends Example", "compareTo$"),
                COMPARE_TO_EXAMPLE));
        scenarios.add(scenario("Override Superclass Method Inherited From Interface, Interface And Superlass Share Parameter",
                classbody("Example<T> implements Comparable<T>", "public int compareTo(Object o) {return 0;} "),
                classbody("SubExample extends Example", "compareTo$"),
                COMPARE_TO_OBJECT));
        scenarios.add(scenario("Override Superclass Method Inherited From Interface, Interface And Class Share Parameter, Class Parameter Bounded",
                classbody("Example<N extends Number> implements Comparable<N>", "public int compareTo(Object o) {return 0;}"),
                classbody("SubExample extends Example", "compareTo$"),
                COMPARE_TO_OBJECT));

        scenarios.add(scenario("Overridden Method Of Object Class",
                classbody("Example", "public int hashCode() { return 0; }"),
                method(" new Example().hashcode$"),
                EXAMPLE_HASH_CODE));
        scenarios.add(scenario("Non-Overridden Method Of Object Class",
                classbody("Example", ""),
                classbody("SubExample extends Example", "void method() { this.hashCode$ }"),
                OBJECT_HASH_CODE));
        scenarios.add(scenario("Method Of Object Class, Overridden by SuperClass",
                classbody("Example", "public int hashCode() { return 0; }"),
                classbody("SubExample extends Example", "void method() { this.hashCode$ }"),
                EXAMPLE_HASH_CODE));
        scenarios.add(scenario("Call This. On Object Class Method, Method Overridden By Current Class And SuperClass",
                classbody("Example", "public int hashCode() { return 0; }"),
                classbody("SubExample extends Example", "public int hashCode() { return 0; } void method() { this.hashCode$ }"),
                SUBEXAMPLE_HASH_CODE));
        scenarios.add(scenario("Call Super. On Object Class Method, Method Overridden By Current Class And SuperClass",
                classbody("Example", "public int hashCode() { return 0; }"),
                classbody("SubExample extends Example", "public int hashCode() { return 0; } void method() { super.hashCode$ }"),
                EXAMPLE_HASH_CODE));
        scenarios.add(scenario("Call Super. On Object Class Method From Anonymous Class, Method Overridden By Anonymous Class And SuperClass",
                classbody("Example", "public int hashCode() { return 0; }"),
                method("new Example() { public int hashCode() { return 0; } void method() { return super.hashCode$ } };"),
                EXAMPLE_HASH_CODE));
        scenarios.add(scenario("Overridden Method Of Object Class, Invoked On Array",
                classbody("Example", "public int hashCode() { return 0; }"),
                method("new Example[0].hashCode$"),
                OBJECT_HASH_CODE));

        // See <https://bugs.eclipse.org/bugs/show_bug.cgi?id=442723>.
        scenarios.add(scenario("Overridden clone method",
                classbody("Example", "public Object clone() { return null; }"),
                method("new Example().clone$"),
                EXAMPLE_CLONE));
        scenarios.add(scenario("Clone method of one-dimensional array",
                classbody("Example", ""),
                method("new Example[0].clone$"),
                OBJECT_CLONE));
        scenarios.add(scenario("Clone method of two-dimensional array",
                classbody("Example", ""),
                method("new Example[0][0].clone$"),
                OBJECT_CLONE));
        // @formatter:on

        return scenarios;
    }

    private static Object[] scenario(String description, CharSequence targetTypeCode,
            CharSequence completionScenarioCode, IMethodName expectedMethod) {
        return new Object[] { false, description, targetTypeCode, completionScenarioCode, expectedMethod };
    }

    /**
     * A scenario defined using this method will work only with JDT 4.5.1 or greater, which addresses
     * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=467902">Bug 467902</a>.
     */
    private static Object[] postJdt451Scenario(String description, CharSequence exampleCU, CharSequence invokingCU,
            IMethodName expectedMethod) {
        return new Object[] { !ProposalUtils.isGetBindingSupported(), description, exampleCU, invokingCU,
                expectedMethod };
    }

    @Test
    public void testSourceBindings() throws Exception {
        TemporaryProject dependency = WORKSPACE.createProject();
        dependency.createFile(targetTypeCode);

        TemporaryProject projectWithSources = WORKSPACE.createProject();
        IRecommendersCompletionContext context = projectWithSources.withDependencyOn(dependency)
                .createFile(completionScenarioCode).triggerContentAssist();

        Collection<CompletionProposal> proposals = context.getProposals().values();
        IMethodName actualMethod = ProposalUtils.toMethodName(getOnlyElement(proposals)).get();

        // Exercise the SUT even if the assumption fails; this helps catch bugs when the above throws exceptions (which
        // it should not).
        assumeThat(ignore, is(equalTo(false)));
        assertThat(actualMethod, is(equalTo(expectedMethod)));
    }

    @Test
    public void testBinaryBindings() throws Exception {
        assumeThat(ignore, is(equalTo(false)));

        TemporaryProject dependency = WORKSPACE.createProject();
        dependency.createFile(targetTypeCode);

        TemporaryProject projectWithSources = WORKSPACE.createProject();
        IRecommendersCompletionContext context = projectWithSources.withDependencyOnClassesOf(dependency)
                .createFile(completionScenarioCode).triggerContentAssist();

        Collection<CompletionProposal> proposals = context.getProposals().values();
        IMethodName actualMethod = ProposalUtils.toMethodName(getOnlyElement(proposals)).get();

        assertThat(actualMethod, is(equalTo(expectedMethod)));
    }
}
