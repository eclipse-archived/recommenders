package org.eclipse.recommenders.utils;

import static org.eclipse.recommenders.utils.Urls.*;
import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class UrlsTest {

    String valid = "http://download.eclipse.org/recommenders/models/2.0/v201210_1212/";
    String valid_escaped = "http___download_eclipse_org_recommenders_models_2_0_v201210_1212_";

    @Test
    public void testMangle() {
        String out = mangle(valid);
        assertEquals(valid_escaped, out);
    }

    @Test
    public void testMangleUrl() {
        String out = mangle(toUrl(valid));
        assertEquals(valid_escaped, out);
    }

    @Test
    public void testToUrl() throws MalformedURLException {
        URL out = toUrl(valid);
        assertEquals(new URL(valid), out);
    }

    @Test(expected = RuntimeException.class)
    public void testToUrFails() {
        Urls.toUrl("http/");
    }
}
