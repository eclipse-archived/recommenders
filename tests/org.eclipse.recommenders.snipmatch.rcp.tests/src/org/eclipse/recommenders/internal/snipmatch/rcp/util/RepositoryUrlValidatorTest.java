package org.eclipse.recommenders.internal.snipmatch.rcp.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
        LinkedList<Object[]> scenarios = new LinkedList<>();
        scenarios.add(invalidUri(""));
        scenarios.add(invalidUri("///"));
        scenarios.add(invalidUri("***"));
        scenarios.add(invalidUri("http://"));
        scenarios.add(invalidUri("http://foo.com"));
        scenarios.add(invalidUri("https:///www.foo.bar/"));
        scenarios.add(invalidUri("http://.."));
        scenarios.add(invalidUri("ssh:user|example.com:my-project"));
        scenarios.add(invalidUri("cvs://folder/"));
        scenarios.add(invalidUri("jar://folder/"));
        scenarios.add(invalidUri("pop://folder/"));
        scenarios.add(invalidUri("telnet://folder/"));
        scenarios.add(invalidUri("udp://folder/"));

        scenarios.add(validUri("http://foo.com/bar_bar"));
        scenarios.add(validUri("https://userid@example.com/"));
        scenarios.add(validUri("http://user:password@folder/"));
        scenarios.add(validUri("http://foo.xz/bar_bar_(foo)_(again)"));
        scenarios.add(validUri("git://host.xz:8001/path/to/repo.git/"));
        scenarios.add(validUri("ssh://git@git.example.com/foo/example.git/"));
        scenarios.add(validUri("amazon-s3://user@fetch/"));
        scenarios.add(validUri("bundle:///"));
        scenarios.add(validUri("file:///"));
        scenarios.add(validUri("ftp://folder/"));
        scenarios.add(validUri("git+ssh://folder/"));
        scenarios.add(validUri("sftp://folder/"));
        scenarios.add(validUri("ssh+git://folder/"));

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
        assertThat(RepositoryUrlValidator.isValidUri(inputUri).isOK(), is(expectedResult));
    }
}
