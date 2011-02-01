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

import static org.junit.Assert.assertArrayEquals;

import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.TreeBag;
import org.junit.Test;

public class TreeBagTest {

    Bag<String> sut = TreeBag.newTreeBag();

    @Test
    public void testKeysetOrder() {
        String[] input = new String[] { "b", "c", "a" };
        String[] expecteds = new String[] { "a", "b", "c" };
        sut.addAll(input);
        Object[] actuals = sut.elements().toArray();
        assertArrayEquals(expecteds, actuals);
    }
}
