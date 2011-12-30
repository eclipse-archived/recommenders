/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.webclient;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.webclient.results.GenericResultObjectView;
import org.eclipse.recommenders.webclient.results.ResultObject;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CouchUtilsTest {

    @Test
    public void testView() {
        final String actual = CouchUtils.createViewUrl("doc", "view");
        assertEquals("_design/doc/_view/view", actual);
    }

    @Test
    public void testViewWithKey() {
        final String actual = CouchUtils.createViewUrlWithKey("doc", "view", "simpleKey");
        assertEquals("_design/doc/_view/view?key=%22simpleKey%22", actual);
    }

    @Test
    public void testViewWithUnescapedKey() {
        final String actual = CouchUtils.createViewUrlWithKey("doc", "view", "simpleK/ey");
        assertEquals("_design/doc/_view/view?key=%22simpleK%2Fey%22", actual);
    }

    @Test
    public void testViewWithUnescapedKeyAndQuote() {
        final String actual = CouchUtils.createViewUrlWithKey("doc", "view", "simple\"Key\"");
        assertEquals("_design/doc/_view/view?key=%22simple%5C%22Key%5C%22%22", actual);
    }

    @Test
    public void testViewWithKeObject() {
        final Map<String, String> keyValuePairs = Maps.newHashMap();
        keyValuePairs.put("abc", "xyz");
        final String actual = CouchUtils.createViewUrlWithKeyObject("doc", "view", keyValuePairs);
        assertEquals("_design/doc/_view/view?key=%7B%22abc%22%3A%22xyz%22%7D", actual);
    }

    @Test
    public void testTransform() {
        final List<String> expected = Lists.newArrayList("value");

        final GenericResultObjectView<String> res = new GenericResultObjectView<String>();
        res.rows = Lists.newArrayList(ResultObject.create("id", "value"));
        final List<String> actual = CouchUtils.transformValues(res);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetFirst() {
        final GenericResultObjectView<String> res = new GenericResultObjectView<String>();
        res.rows = Lists.newArrayList(ResultObject.create("id", "value"));
        final String actual = CouchUtils.getFirst(res, null);
        assertEquals("value", actual);
    }

    @Test
    public void testGetFirstWithDefault() {
        final GenericResultObjectView<String> res = new GenericResultObjectView<String>();
        res.rows = Lists.newArrayList();
        final String actual = CouchUtils.getFirst(res, "default");
        assertEquals("default", actual);
    }
}
