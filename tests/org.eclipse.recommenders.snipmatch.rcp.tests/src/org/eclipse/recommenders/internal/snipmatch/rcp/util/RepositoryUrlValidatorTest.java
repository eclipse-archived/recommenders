package org.eclipse.recommenders.internal.snipmatch.rcp.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class RepositoryUrlValidatorTest {

    private final String inputUri;
    private final boolean expectedResult;

    public RepositoryUrlValidatorTest(String inputString, boolean expectedResult) {
        this.inputUri = inputString;
        this.expectedResult = expectedResult;
    }

    @Parameters
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(invalidUri(""));
        scenarios.add(invalidUri("http://"));
        scenarios.add(invalidUri("http://foo.com"));
        scenarios.add(invalidUri("https:///www.foo.bar/"));
        scenarios.add(invalidUri("http://.."));
        scenarios.add(invalidUri("ssh://serverexample.com@example.com:/home/git.example.com/example.git"));

        scenarios.add(validUri("http://foo.com/bar_bar"));
        scenarios.add(validUri("https://userid@example.com/"));
        scenarios.add(validUri("http://foo.xz/bar_bar_(foo)_(again)"));
        scenarios.add(validUri("git://host.xz:8001/path/to/repo.git/"));
        scenarios.add(validUri("ssh://git@git.example.com/foo/example.git/"));

        return scenarios;
    }

    private static Object[] invalidUri(String uri) {
        return new Object[] { uri, false };
    }

    private static Object[] validUri(String uri) {
        return new Object[] { uri, true };
    }

    @Test
    public void testValidateUri() {
        assertThat(RepositoryUrlValidator.isValidUri(inputUri), is(expectedResult));
    }
}
