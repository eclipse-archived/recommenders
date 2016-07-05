package org.eclipse.recommenders.utils;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Collections;

import org.junit.Test;

public class UrisTest {

    private static final String HTTP_ABSOLUTE_URI = "http://download.eclipse.org/recommenders/models/2.0/v201210_1212/data";
    private static final String HTTPS_ABSOLUTE_URI = "https://download.eclipse.org/recommenders/models/2.0/v201210_1212/";
    private static final String RELATIVE_URI = "download.eclipse.org/recommenders/models/2.0/v201210_1212/";

    @Test
    public void testParseUriValidAbsoluteUri() throws Exception {
        URI expectedUri = new URI(HTTP_ABSOLUTE_URI);

        assertThat(Uris.parseURI(HTTP_ABSOLUTE_URI).get(), is(expectedUri));
    }

    @Test
    public void testParseUriValidRelativeUri() throws Exception {
        URI expectedUri = new URI(RELATIVE_URI);
        assertThat(Uris.parseURI(RELATIVE_URI).get(), is(expectedUri));
    }

    @Test
    public void testParseUriEmptyUri() throws Exception {
        URI uri = new URI("");
        assertThat(Uris.parseURI("").get(), is(uri));
    }

    @Test
    public void testParseUriInvalidUri() throws Exception {
        assertThat(Uris.parseURI("<>").isPresent(), is(false));
    }

    @Test
    public void testValidAbsoluteUriWithSupportedProtocol() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        assertThat(Uris.isUriProtocolSupported(uri, asList("http", "file", "https")), is(true));
    }

    @Test
    public void testValidRelativeUriProtocol() throws Exception {
        URI uri = new URI(RELATIVE_URI);
        assertThat(Uris.isUriProtocolSupported(uri, asList("http", "file", "https")), is(false));
    }

    @Test
    public void testValidAbsoluteUriWithUnsupportedProtocol() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        assertThat(Uris.isUriProtocolSupported(uri, asList("file")), is(false));
    }

    @Test
    public void testValidAbsoluteUriWithEmptyProtocolList() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        assertThat(Uris.isUriProtocolSupported(uri, Collections.<String>emptyList()), is(false));
    }

    @Test
    public void testLowerUpperCaseValidAbsoluteUriWithSupportedProtocol() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        assertThat(Uris.isUriProtocolSupported(uri, asList("HTTP", "file", "https")), is(true));
    }

    @Test
    public void testAbsoluteUriProtocolIsSubstringOfSupportedProtocol() throws Exception {
        URI uri = new URI(HTTPS_ABSOLUTE_URI);
        assertThat(Uris.isUriProtocolSupported(uri, asList("http")), is(false));
    }

    @Test
    public void testToStringWithMaskedPassword() throws Exception {
        assertThat(Uris.toStringWithMaskedPassword(new URI("http://example.org/path"), '*'),
                is(equalTo("http://example.org/path")));
    }

    @Test
    public void testToStringWithMaskedPasswordOpaqueUri() throws Exception {
        assertThat(Uris.toStringWithMaskedPassword(new URI("mailto:postmaster@example.org"), '*'),
                is(equalTo("mailto:postmaster@example.org")));
    }

    @Test
    public void testToStringWithMaskedPasswordUriWithUsername() throws Exception {
        assertThat(Uris.toStringWithMaskedPassword(new URI("http://username@example.org/path"), '*'),
                is(equalTo("http://username@example.org/path")));
    }

    @Test
    public void testToStringWithMaskedPasswordUriWithUsernameAndPassword() throws Exception {
        assertThat(Uris.toStringWithMaskedPassword(new URI("http://username:password@example.org/path"), '*'),
                is(equalTo("http://username:********@example.org/path")));
    }

    @Test
    public void testToStringWithMaskedPasswordUriWithUsernameAndEmptyPassword() throws Exception {
        assertThat(Uris.toStringWithMaskedPassword(new URI("http://username:@example.org/path"), '*'),
                is(equalTo("http://username:@example.org/path")));
    }

    @Test
    public void testToStringWithoutUserInfo() throws Exception {
        assertThat(Uris.toStringWithoutUserinfo(new URI("http://example.org/path")),
                is(equalTo("http://example.org/path")));
    }

    @Test
    public void testToStringWithoutUserInfoOpaqueUri() throws Exception {
        assertThat(Uris.toStringWithoutUserinfo(new URI("mailto:postmaster@example.org")),
                is(equalTo("mailto:postmaster@example.org")));
    }

    @Test
    public void testToStringWithoutUserInfoUriWithUsername() throws Exception {
        assertThat(Uris.toStringWithoutUserinfo(new URI("http://username@example.org/path")),
                is(equalTo("http://example.org/path")));
    }

    @Test
    public void testToStringWithoutUserInfoUriWithUsernameAndPassword() throws Exception {
        assertThat(Uris.toStringWithoutUserinfo(new URI("http://username:password@example.org/path")),
                is(equalTo("http://example.org/path")));
    }

    @Test
    public void testToStringWithoutUserInfoUriWithUsernameAndEmptyPassword() throws Exception {
        assertThat(Uris.toStringWithoutUserinfo(new URI("http://username:@example.org/path")),
                is(equalTo("http://example.org/path")));
    }
}
