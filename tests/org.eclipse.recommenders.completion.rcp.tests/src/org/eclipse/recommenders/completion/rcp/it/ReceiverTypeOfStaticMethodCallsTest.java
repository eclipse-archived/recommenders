package org.eclipse.recommenders.completion.rcp.it;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Test that receiver types of static method calls are handled correctly.
 */
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

        IRecommendersCompletionContext sut = exercise(code);
        IType receiverType = sut.getReceiverType().get();

        assertThat(receiverType.getElementName(), is(equalTo(type)));
    }

    private IRecommendersCompletionContext exercise(CharSequence code) throws CoreException {
        JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");
        Pair<ICompilationUnit, Set<Integer>> struct = fixture.createFileAndParseWithMarkers(code.toString());
        ICompilationUnit cu = struct.getFirst();
        int completionIndex = struct.getSecond().iterator().next();
        JavaContentAssistInvocationContext ctx = new JavaContentAssistContextMock(cu, completionIndex);

        return new RecommendersCompletionContext(ctx, new CachingAstProvider());
    }
}
