package org.eclipse.recommenders.internal.snipmatch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FilenamesTest {

    private final String filename;
    private final List<String> expected;

    public FilenamesTest(String description, String filename, List<String> expected) {
        this.filename = filename;
        this.expected = expected;
    }

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = new LinkedList<>();

        scenarios.add(scenario("null filename", null));
        scenarios.add(scenario("No extension", "name", "name"));
        scenarios.add(scenario("One extension", "name.extension", "name.extension", ".extension"));
        scenarios.add(scenario("Two extensions", "name.first.second", "name.first.second", ".first.second", ".second"));
        scenarios.add(scenario("Extension only", ".gitignore", ".gitignore"));
        scenarios.add(scenario("Two dots", "name..extension", "name..extension", "..extension", ".extension"));
        scenarios.add(scenario("Two dots at beginning", "..extension", "..extension", ".extension"));
        return scenarios;
    }

    private static Object[] scenario(String description, String filename, String... expected) {
        return new Object[] { description, filename, Arrays.asList(expected) };
    }

    @Test
    public void test() {
        List<String> actual = Filenames.getFilenameRestrictions(filename);

        assertThat(actual, is(equalTo(expected)));
    }
}
