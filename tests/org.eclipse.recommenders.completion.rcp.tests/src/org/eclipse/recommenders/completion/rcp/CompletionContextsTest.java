package org.eclipse.recommenders.completion.rcp;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class CompletionContextsTest {

    private String displayString;
    private String expected;

    public CompletionContextsTest(String displayString, String expected) {
        this.displayString = displayString;
        this.expected = expected;
    }

    @Parameters
    public static Collection<Object[]> input() {
        LinkedList<Object[]> data = Lists.newLinkedList();
        data.add(new String[] { "blockedHandler : Dialog", "blockedHandler" });
        data.add(new String[] { "layout(boolean changed)", "layout" });
        data.add(new String[] { "add(Object o) : Object", "add" });
        data.add(new String[] { "ArrayList(Collection<? extends String> c)", "ArrayList" });
        data.add(new String[] { "org.eclipse.package", "org.eclipse.package" });
        return data;
    }

    @Test
    public void test() {
        final String actual = CompletionContexts.getPrefixMatchingArea(displayString);
        assertEquals(expected, actual);
    }
}
