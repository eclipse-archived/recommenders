package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SnippetSourceValidatorTest {

    private static final boolean VALID = true;
    private static final boolean INVALID = false;

    private final String templateText;
    private final boolean isSourceValidAsJavaSnippet;
    private final boolean isSourceValidAsTestSnippet;

    public SnippetSourceValidatorTest(String description, String templateText, boolean isSourceValidAsJavaSnippet,
            boolean isSourceValidAsTestSnippet) {
        this.templateText = templateText;
        this.isSourceValidAsJavaSnippet = isSourceValidAsJavaSnippet;
        this.isSourceValidAsTestSnippet = isSourceValidAsTestSnippet;
    }

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = new LinkedList<>();

        scenarios.add(scenario("Empty snippet", "", VALID, VALID));
        scenarios.add(scenario("Simple snippet", "Lorem ipsum dolor", VALID, VALID));
        scenarios.add(scenario("Snippet with id", "${id}", VALID, VALID));
        scenarios.add(scenario("Snippet with var reference", "${id:var(java.lang.String)}", VALID, VALID));
        scenarios.add(scenario("Snippet with escaped dollar sign", "$$", VALID, VALID));
        scenarios.add(scenario("Snippet with single dollar sign", "$", INVALID, INVALID));
        scenarios.add(scenario("Unclosed var reference", "${id:var(java.lang.String)", INVALID, INVALID));

        return scenarios;
    }

    private static Object[] scenario(String description, String templateText, boolean isSourceValidAsJavaSnippet,
            boolean isSourceValidAsTestSnippet) {
        return new Object[] { description, templateText, isSourceValidAsJavaSnippet, isSourceValidAsTestSnippet };
    }

    @Test
    public void testIsSourceValidAsJavaSnippet() {
        assertThat(JavaSnippetSourceValidator.isSourceValid(templateText).isEmpty(), is(isSourceValidAsJavaSnippet));
    }

    @Test
    public void testIsSourceValidAsTextSnippet() {
        assertThat(TextSnippetSourceValidator.isSourceValid(templateText).isEmpty(), is(isSourceValidAsTestSnippet));
    }
}
