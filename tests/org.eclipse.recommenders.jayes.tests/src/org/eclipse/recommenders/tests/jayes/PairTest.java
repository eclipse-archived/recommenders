/*******************************************************************************
 * Copyright (c) 2013 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.tests.jayes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.recommenders.jayes.util.OrderIgnoringPair;
import org.eclipse.recommenders.jayes.util.Pair;
import org.junit.Test;

public class PairTest {
    @Test
    public void testOrderIgnoringPairIgnoresOrder() {

        OrderIgnoringPair<Object> _21 = new OrderIgnoringPair<Object>(2, 1);
        OrderIgnoringPair<Object> _12 = new OrderIgnoringPair<Object>(1, 2);
        assertThat(_12, is(_21));
        assertThat(_12.hashCode(), is(_21.hashCode()));

    }

    @Test
    public void testPairRespectsOrder() {
        assertThat(Pair.newPair(1, 2), is(not(Pair.newPair(2, 1))));

    }
}
