package org.eclipse.recommenders.completion.rcp.processable;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class SimpleProposalProcessorTest<T> {

    private final T first;
    private final T second;
    private final boolean expected;

    public SimpleProposalProcessorTest(String description, T first, T second, boolean equals) {
        this.first = first;
        this.second = second;
        this.expected = equals;
    }

    @Parameters(name = "{index}: {0} => equal: {3}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("Same increment, no label", new SimpleProposalProcessor(1), new SimpleProposalProcessor(
                1), true));
        scenarios.add(scenario("Same increment, same label", new SimpleProposalProcessor(1, "one"),
                new SimpleProposalProcessor(1, "one"), true));
        scenarios.add(scenario("Same increment, different label", new SimpleProposalProcessor(1, "1"),
                new SimpleProposalProcessor(1, "one"), false));
        scenarios.add(scenario("Same increment, just one label", new SimpleProposalProcessor(1),
                new SimpleProposalProcessor(1, "one"), false));

        scenarios.add(scenario("Different increment, no label", new SimpleProposalProcessor(1),
                new SimpleProposalProcessor(2), false));
        scenarios.add(scenario("Different increment, same label", new SimpleProposalProcessor(1, "one"),
                new SimpleProposalProcessor(2, "one"), false));
        scenarios.add(scenario("Different increment, different label", new SimpleProposalProcessor(1, "one"),
                new SimpleProposalProcessor(2, "two"), false));
        scenarios.add(scenario("Different increment, just one label", new SimpleProposalProcessor(1),
                new SimpleProposalProcessor(2, "two"), false));

        return scenarios;
    }

    @Test
    public void testEquals() {
        assertThat(first.equals(second), is(expected));
        assertThat(second.equals(first), is(expected));
    }

    @Test
    public void testHashCode() {
        assumeThat(first, is(equalTo(second)));
        assertThat(first.hashCode(), is(equalTo(second.hashCode())));
    }

    private static <T> Object[] scenario(String description, T first, T second, boolean equals) {
        return new Object[] { description, first, second, equals };
    }
}
