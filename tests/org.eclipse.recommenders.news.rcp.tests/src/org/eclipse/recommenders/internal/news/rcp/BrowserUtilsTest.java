/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.BrowserUtils.encodeURI;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class BrowserUtilsTest {

    private static final NameValuePair VALID_PARAMETER = new BasicNameValuePair("parameter", "value");
    private static final String PARAMETER = "parameter";
    private static final String PARAMETER_A = "parameterA";
    private static final String VALUE = "value";
    private static final String BASE_URL = "http://eclipse.org";
    private static final String BASE_URL_WITH_SPACE = "http://eclipse.org";
    private static final String BASE_URL_WITH_PARAMETER = "http://eclipse.org?foo=bar";

    private final String actualUrl;
    private final Map<String, String> parameters;
    private final String expectedUrl;

    @Parameters
    public static Collection<Object[]> scenarios() {
        List<Object[]> scenarios = Lists.newArrayList();

        scenarios.add(new Object[] { null, null, "" });
        scenarios.add(new Object[] { BASE_URL, null, BASE_URL });
        scenarios.add(new Object[] { BASE_URL, ImmutableMap.of(PARAMETER, VALUE), BASE_URL + "?" + VALID_PARAMETER });
        scenarios.add(new Object[] { BASE_URL_WITH_PARAMETER, ImmutableMap.of(PARAMETER, VALUE),
                BASE_URL_WITH_PARAMETER + "&" + VALID_PARAMETER });
        scenarios.add(new Object[] { BASE_URL, ImmutableMap.of(PARAMETER, VALUE, PARAMETER_A, VALUE),
                BASE_URL + "?" + VALID_PARAMETER + "&" + PARAMETER_A + "=" + VALUE });
        scenarios.add(new Object[] { BASE_URL_WITH_SPACE, ImmutableMap.of(PARAMETER, VALUE),
                BASE_URL + "?" + VALID_PARAMETER });

        return scenarios;
    }

    public BrowserUtilsTest(String url, Map<String, String> parameters, String expectedUrl) {
        actualUrl = url;
        this.parameters = parameters;
        this.expectedUrl = expectedUrl;
    }

    @Test
    public void testEncodeURI() throws Exception {
        assertThat(encodeURI(actualUrl, parameters), is(Matchers.equalTo(expectedUrl)));
    }
}
