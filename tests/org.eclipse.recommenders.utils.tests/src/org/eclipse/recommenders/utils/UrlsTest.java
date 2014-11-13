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

    private static final String ESCAPED_HTTP_ABSOLUTE_URI = "http___download_eclipse_org_recommenders_models_2_0_v201210_1212_";
    private static final String HTTP_ABSOLUTE_URI = "http://download.eclipse.org/recommenders/models/2.0/v201210_1212/";
    private static final String HTTPS_ABSOLUTE_URI = "https://download.eclipse.org/recommenders/models/2.0/v201210_1212/";
    private static final String RELATIVE_URI = "download.eclipse.org/recommenders/models/2.0/v201210_1212/";

    @Test
    public void testMangle() {
        String out = mangle(HTTP_ABSOLUTE_URI);
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testMangleUrl() {
        String out = mangle(toUrl(HTTP_ABSOLUTE_URI));
        assertEquals(ESCAPED_HTTP_ABSOLUTE_URI, out);
    }

    @Test
    public void testToUrl() throws MalformedURLException {
        URL out = toUrl(HTTP_ABSOLUTE_URI);
        assertEquals(new URL(HTTP_ABSOLUTE_URI), out);
    }

    @Test(expected = RuntimeException.class)
    public void testToUrFails() {
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
}
