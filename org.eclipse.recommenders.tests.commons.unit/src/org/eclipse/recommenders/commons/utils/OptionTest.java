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
package org.eclipse.recommenders.commons.utils;

import static org.eclipse.recommenders.commons.utils.Option.wrap;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class OptionTest {

    private List<Option<String>> names = null;

    @Before
    public void setup() {
        names = Lists.newArrayList(wrap("Dean"), wrap((String) null), wrap("Wampler"));
    }

    @Test
    public void getOrElseUsesValueForSomeAndAlternativeForNone() {
        final String[] expected = { "Dean", "Unknown!", "Wampler" };

        for (int i = 0; i < names.size(); i++) {
            final Option<String> name = names.get(i);
            final String value = name.getOrElse("Unknown!");
            assertEquals(expected[i], value);
        }
    }

    @Test
    public void hasNextWithGetUsesOnlyValuesForSomes() {
        final String[] expected = { "Dean", null, "Wampler" };
        for (int i = 0; i < names.size(); i++) {
            final Option<String> name = names.get(i);
            if (name.hasValue()) {
                final String value = name.get();
                assertEquals(expected[i], value);
            }
        }
    }
}
