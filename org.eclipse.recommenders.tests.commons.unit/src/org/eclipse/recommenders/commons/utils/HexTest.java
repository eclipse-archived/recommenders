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
package org.eclipse.recommenders.commons.utils;

import junit.framework.Assert;

import org.junit.Test;

public class HexTest {

    @Test
    public void testHex() {
        // setup:
        final String hexString = "ABCDEF01234897";

        // exercise:
        final String result = Hex.asHexString(Hex.asByteArray(hexString));

        // verify:
        Assert.assertEquals(hexString, result);
    }
}
