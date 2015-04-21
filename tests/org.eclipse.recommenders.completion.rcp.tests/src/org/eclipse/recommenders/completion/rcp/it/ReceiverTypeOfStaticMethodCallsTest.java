package org.eclipse.recommenders.completion.rcp.it;

import static org.eclipse.recommenders.completion.rcp.it.TestUtils.createRecommendersCompletionContext;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.testing.CodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Test that receiver types of static method calls are handled correctly.
 */
@SuppressWarnings({ "restriction" })
@RunWith(Parameterized.class)
public class ReceiverTypeOfStaticMethodCallsTest {

    private final String type;

    public ReceiverTypeOfStaticMethodCallsTest(String type) {
        this.type = type;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("System"));
        scenarios.add(scenario("Class")); // <T>
        scenarios.add(scenario("AtomicReferenceFieldUpdater")); // <T, V>

        return scenarios;
    }

    private static Object[] scenario(String type) {
        return new Object[] { type };
    }

    @Test
    public void testReceiverTypeOfStaticMethodCall() throws Exception {
        CharSequence code = CodeBuilder.method(type + ".$;");

        IRecommendersCompletionContext sut = createRecommendersCompletionContext(code);
        IType receiverType = sut.getReceiverType().get();

        assertThat(receiverType.getElementName(), is(equalTo(type)));
    }
}
