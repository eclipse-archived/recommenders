package org.eclipse.recommenders.completion.rcp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class CompletionContextsTest {

    private final String displayString;
    private final String expectedPrefixMatchingArea;

    public CompletionContextsTest(String displayString, String expectedPrefixMatchingArea) {
        this.displayString = displayString;
        this.expectedPrefixMatchingArea = expectedPrefixMatchingArea;
    }

    @Parameters
    public static Iterable<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(new String[] { "blockedHandler : Dialog", "blockedHandler" });

        scenarios.add(new String[] { "layout(boolean changed)", "layout" });
        scenarios.add(new String[] { "add(Object o) : Object", "add" });

        scenarios.add(new String[] { "ArrayList ()VLjava.util.ArrayList<TE;>;", "ArrayList" });
        scenarios.add(new String[] { "ZipFile (Ljava.io.File;I)VLjava.util.zip.ZipFile;", "ZipFile" });

        scenarios.add(new String[] { "List() Anonymous Inner Type - java.awt.geom", "List" });

        scenarios.add(new String[] { "org.eclipse.package", "org.eclipse.package" });

        scenarios.add(new String[] { "@deprecated", "@deprecated" });
        scenarios.add(new String[] { "{@value}", "{@value}" });

        scenarios.add(new String[] { "Override Ljava.lang.Override;", "Override" });

        return scenarios;
    }

    @Test
    public void testGetPrefixMatchingArea() {
        assertThat(CompletionContexts.getPrefixMatchingArea(displayString), is(equalTo(expectedPrefixMatchingArea)));
    }
}
