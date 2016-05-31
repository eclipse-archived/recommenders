package org.eclipse.recommenders.utils;

import static org.eclipse.recommenders.utils.Urls.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

public class UrlsTest {

    private static final String ESCAPED_HTTP_ABSOLUTE_URI = "http___download_eclipse_org_recommenders_models_2_0_v201210_1212_data";
    private static final String ESCAPED_HTTP_ABSOLUTE_URI_WITH_PORT = "http___download_eclipse_org_123_recommenders_models_2_0_v201210_1212_data_key_value_fragid1";
    private static final String ESCAPED_HTTP_ABSOLUTE_URI_WITH_QUERY = "http___download_eclipse_org_recommenders_models_2_0_v201210_1212_data_key_value";
    private static final String ESCAPED_HTTP_ABSOLUTE_URI_WITH_QUERY_AND_FRAGMENT = "http___download_eclipse_org_recommenders_models_2_0_v201210_1212_data_key_value_fragid1";
    private static final String ESCAPED_RELATIVE_URI = "download_eclipse_org_recommenders_models_2_0_v201210_1212_";

    private static final String HTTP_ABSOLUTE_URI = "http://download.eclipse.org/recommenders/models/2.0/v201210_1212/data";
    private static final String HTTP_ABSOLUTE_URI_WITH_PORT = "http://download.eclipse.org:123/recommenders/models/2.0/v201210_1212/data?key=value#fragid1";
    private static final String HTTP_ABSOLUTE_URI_WITH_QUERY = "http://download.eclipse.org/recommenders/models/2.0/v201210_1212/data?key=value";
    private static final String HTTP_ABSOLUTE_URI_WITH_QUERY_AND_FRAGMENT = "http://download.eclipse.org/recommenders/models/2.0/v201210_1212/data?key=value#fragid1";
    private static final String HTTP_ABSOLUTE_URI_WITH_USERNAME = "http://user@download.eclipse.org/recommenders/models/2.0/v201210_1212/data";
    private static final String HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_PASSWORD = "http://user:password@download.eclipse.org/recommenders/models/2.0/v201210_1212/data";
    private static final String HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_MASKED_PASSWORD = "http://user:********@download.eclipse.org/recommenders/models/2.0/v201210_1212/data";
    private static final String HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_MASKED_PASSWORD_DIFFERENT_MASK = "http://user:xxxxxxxx@download.eclipse.org/recommenders/models/2.0/v201210_1212/data";
    private static final String HTTPS_ABSOLUTE_URI = "https://download.eclipse.org/recommenders/models/2.0/v201210_1212/";
    private static final String RELATIVE_URI = "download.eclipse.org/recommenders/models/2.0/v201210_1212/";

    @Test
    public void testMangleUrl() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testMangleUrlWithPort() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI_WITH_PORT));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI_WITH_PORT, out);
    }

    @Test
    public void testMangleUrlWithQuery() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI_WITH_QUERY));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI_WITH_QUERY, out);
    }

    @Test
    public void testMangleUrlWithQueryAndFragment() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI_WITH_QUERY_AND_FRAGMENT));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI_WITH_QUERY_AND_FRAGMENT, out);
    }

    @Test
    public void testMangleUrlWithUsername() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testMangleUrlWithUsernameAndPassword() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_PASSWORD));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testMangleUrlAsString() {
        String out = mangle(HTTP_ABSOLUTE_URI);
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testMangleUrlWithPortAsString() {
        String out = mangle(HTTP_ABSOLUTE_URI_WITH_PORT);
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI_WITH_PORT, out);
    }

    @Test
    public void testMangleUrlWithQueryAsString() {
        String out = mangle(HTTP_ABSOLUTE_URI_WITH_QUERY);
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI_WITH_QUERY, out);
    }

    @Test
    public void testMangleUrlWithQueryAndFragmentAsString() {
        String out = mangle(HTTP_ABSOLUTE_URI_WITH_QUERY_AND_FRAGMENT);
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI_WITH_QUERY_AND_FRAGMENT, out);
    }

    @Test
    public void testMangleUrlWithUsernameAsString() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testMangleUrlWithUsernameAndPasswordAsString() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_PASSWORD));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testMangleRelativeUriAsString() {
        String out = mangle(RELATIVE_URI);
        assertEquals(ESCAPED_RELATIVE_URI, out);
    }

    @Test
    public void testToUrl() throws MalformedURLException {
        URL out = toUrl(HTTP_ABSOLUTE_URI);
        assertEquals(new URL(HTTP_ABSOLUTE_URI), out);
    }

    @Test(expected = RuntimeException.class)
    public void testToUrlFails() {
        Urls.toUrl("http/");
    }

    @Test
    public void testValidAbsoluteUri() throws Exception {
        URI expectedUri = new URI(HTTP_ABSOLUTE_URI);

        assertThat(parseURI(HTTP_ABSOLUTE_URI).get(), is(expectedUri));
    }

    @Test
    public void testValidRelativeUri() throws Exception {
        URI expectedUri = new URI(RELATIVE_URI);
        assertThat(parseURI(RELATIVE_URI).get(), is(expectedUri));
    }

    @Test
    public void testEmptyUri() throws Exception {
        URI uri = new URI("");
        assertThat(parseURI("").get(), is(uri));
    }

    @Test
    public void testInvalidUri() throws Exception {
        assertEquals(parseURI("<>").isPresent(), false);
    }

    @Test
    public void testValidAbsoluteUriWithSupportedProtocol() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        ArrayList<String> acceptedProtocols = new ArrayList<String>();
        acceptedProtocols.add("http");
        acceptedProtocols.add("file");
        acceptedProtocols.add("https");
        assertEquals(isUriProtocolSupported(uri, acceptedProtocols), true);
    }

    @Test
    public void testValidRelativeUriProtocol() throws Exception {
        URI uri = new URI(RELATIVE_URI);
        ArrayList<String> acceptedProtocols = new ArrayList<String>();
        acceptedProtocols.add("http");
        acceptedProtocols.add("file");
        acceptedProtocols.add("https");
        assertEquals(isUriProtocolSupported(uri, acceptedProtocols), false);
    }

    @Test
    public void testValidAbsoluteUriWithUnsupportedProtocol() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        ArrayList<String> acceptedProtocols = new ArrayList<String>();
        acceptedProtocols.add("file");
        assertEquals(isUriProtocolSupported(uri, acceptedProtocols), false);
    }

    @Test
    public void testValidAbsoluteUriWithEmptyProtocolList() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        assertEquals(isUriProtocolSupported(uri, new ArrayList<String>()), false);
    }

    @Test
    public void testLowerUpperCaseValidAbsoluteUriWithSupportedProtocol() throws Exception {
        URI uri = new URI(HTTP_ABSOLUTE_URI);
        ArrayList<String> acceptedProtocols = new ArrayList<String>();
        acceptedProtocols.add("HTTP");
        acceptedProtocols.add("file");
        acceptedProtocols.add("https");
        assertEquals(isUriProtocolSupported(uri, acceptedProtocols), true);
    }

    @Test
    public void testAbsoluteUriProtocolIsSubstringOfSupportedProtocol() throws Exception {
        URI uri = new URI(HTTPS_ABSOLUTE_URI);
        ArrayList<String> acceptedProtocols = new ArrayList<String>();
        acceptedProtocols.add("file");
        acceptedProtocols.add("http");
        assertEquals(isUriProtocolSupported(uri, acceptedProtocols), false);
    }

    @Test
    public void testUrlToStringWithoutUsernameAndPassword() {
        String out = toStringWithoutUsernameAndPassword(toUrl(HTTP_ABSOLUTE_URI));
        assertEquals(HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testUrlWithUsernameToStringWithoutUsernameAndPassword() {
        String out = toStringWithoutUsernameAndPassword(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME));
        assertEquals(HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testUrlWithUsernameAndPasswordToStringWithoutUsernameAndPassword() {
        String out = toStringWithoutUsernameAndPassword(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_PASSWORD));
        assertEquals(HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testUrlToStringWithMaskedPassword() {
        String out = toStringWithMaskedPassword(toUrl(HTTP_ABSOLUTE_URI), '*');
        assertEquals(HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testUrlWithUsernameToStringWithMaskedPassword() {
        String out = toStringWithMaskedPassword(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME), '*');
        assertEquals(HTTP_ABSOLUTE_URI_WITH_USERNAME, out);
    }

    @Test
    public void testUrlWithUsernameAndPasswordToStringWithMaskedPassword() {
        String out = toStringWithMaskedPassword(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_PASSWORD), '*');
        assertEquals(HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_MASKED_PASSWORD, out);
    }

    @Test
    public void testUrlWithUsernameAndPasswordToStringWithMaskedPasswordDifferentMask() {
        String out = toStringWithMaskedPassword(toUrl(HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_PASSWORD), 'x');
        assertEquals(HTTP_ABSOLUTE_URI_WITH_USERNAME_AND_MASKED_PASSWORD_DIFFERENT_MASK, out);
    }
}
