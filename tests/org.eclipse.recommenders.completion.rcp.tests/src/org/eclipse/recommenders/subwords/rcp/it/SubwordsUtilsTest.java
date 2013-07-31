/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.subwords.rcp.it;

import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.internal.subwords.rcp.SubwordsUtils;
import org.junit.Test;

public class SubwordsUtilsTest {

    @Test
    public void testFieldCompletion() {
        final String test = "blockedHandler : Dialog";
        final String expected = "blockedHandler";
        final String actual = SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket(test);
        assertEquals(expected, actual);
    }

    @Test
    public void testCallCompletion() {
        final String test = "layout(boolean changed)";
        final String expected = "layout";
        final String actual = SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket(test);
        assertEquals(expected, actual);
    }

}
