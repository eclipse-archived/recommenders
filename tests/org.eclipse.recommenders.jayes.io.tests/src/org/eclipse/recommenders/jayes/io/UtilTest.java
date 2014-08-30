/*******************************************************************************
 * Copyright (c) 2015 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.jayes.io;

import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.internal.jayes.io.util.XMLUtil;
import org.junit.Test;

public class UtilTest {

    @Test
    public void testSurround() {
        String text = "this is text";
        String expected = "this is <tag id=\"test\" >text</tag>";

        StringBuilder bldr = new StringBuilder();
        bldr.append(text);
        XMLUtil.surround(text.indexOf("text"), bldr, "tag", "id", "test");
        assertEquals(expected, bldr.toString());
    }

    @Test
    public void testEmptyTag() {
        String text = "this is text";
        String expected = "this is text<br attribute=\"attr\" />";

        StringBuilder bldr = new StringBuilder();
        bldr.append(text);
        XMLUtil.emptyTag(bldr, "br", "attribute", "attr");
        assertEquals(expected, bldr.toString());
    }

    @Test
    public void testEscape() {
        String toEscape = "Ljava/beans/BeanInfo.getAdditionalBeanInfo()[Ljava/Fbeans/BeanInfo;";
        String escaped = "Ljava_2Fbeans_2FBeanInfo_2EgetAdditionalBeanInfo_28_29_5BLjava_2FFbeans_2FBeanInfo_3B";
        assertEquals(escaped, XMLUtil.escape(toEscape));
        assertEquals(toEscape, XMLUtil.unescape(escaped));
    }

}
