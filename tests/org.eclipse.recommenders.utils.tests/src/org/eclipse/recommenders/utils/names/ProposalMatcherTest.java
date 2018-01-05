package org.eclipse.recommenders.utils.names;

import static org.eclipse.recommenders.utils.names.VmMethodName.get;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ProposalMatcherTest {

    private final IMethodName proposedMethod;
    private final IMethodName candidateMethod;
    private final boolean match;

    public ProposalMatcherTest(String description, IMethodName proposedMethod, IMethodName candidateMethod,
            boolean match) {
        this.proposedMethod = proposedMethod;
        this.candidateMethod = candidateMethod;
        this.match = match;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = new LinkedList<>();

        scenarios.add(mismatch("Null", get("Lorg/example/Example.method()V"), null));

        scenarios.add(mismatch("Different names", get("Lorg/example/Example.method()V"),
                get("Lorg/example/Example.other()V")));
        scenarios.add(mismatch("Different number of parameters", get("Lorg/example/Example.method()V"),
                get("Lorg/example/Example.method(I)V")));
        scenarios.add(mismatch("Different type of parameter (int vs. long)", get("Lorg/example/Example.method(I)V"),
                get("Lorg/example/Example.method(J)V")));
        scenarios.add(mismatch("Different type of parameter (object vs. array)",
                get("Lorg/example/Example.method(Ljava/lang/Object;)V"),
                get("Lorg/example/Example.method([Ljava/lang/Object;)V")));
        scenarios.add(mismatch("Different type of parameter (different array dimensions)",
                get("Lorg/example/Example.method([Ljava/lang/Object;)V"),
                get("Lorg/example/Example.method([[Ljava/lang/Object;)V")));
        scenarios.add(mismatch("Different order of parameters", get("Lorg/example/Example.method(IJ)V"),
                get("Lorg/example/Example.method(JI)V")));

        scenarios.add(match("Different declaring class doesn't matter", get("Lorg/example/Example.method()V"),
                get("Lorg/example/Example.method()V")));
        scenarios.add(match("Different return type doesn't matter", get("Lorg/example/Example.method()V"),
                get("Lorg/example/Example.method()I")));

        return scenarios;
    }

    private static Object[] mismatch(String description, IMethodName proposedMethod, IMethodName candidateMethod) {
        return new Object[] { description, proposedMethod, candidateMethod, false };
    }

    private static Object[] match(String description, IMethodName proposedMethod, IMethodName candidateMethod) {
        return new Object[] { description, proposedMethod, candidateMethod, true };
    }

    @Test
    public void test() throws Exception {
        ProposalMatcher sut = new ProposalMatcher(proposedMethod);

        assertThat(sut.match(candidateMethod), is(equalTo(match)));
    }
}
