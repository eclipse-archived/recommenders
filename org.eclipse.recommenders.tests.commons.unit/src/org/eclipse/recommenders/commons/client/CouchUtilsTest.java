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
package org.eclipse.recommenders.commons.client;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

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
    public void testTransform() {
        final List<String> expected = Lists.newArrayList("value");

        final GenericResultObjectView<String> res = new GenericResultObjectView<String>();
        res.rows = Lists.newArrayList(ResultObject.create("id", "value"));
        final List<String> actual = CouchUtils.transform(res);
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
