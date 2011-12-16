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
package org.eclipse.recommenders.utils;

import static org.junit.Assert.assertArrayEquals;

import java.util.Comparator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

public class TreeBagTest {

    @Test
    public void testKeysetOrder() {
        final Bag<String> sut = TreeBag.newTreeBag();
        final String[] input = new String[] { "b", "c", "a" };
        final String[] expecteds = new String[] { "a", "b", "c" };
        sut.addAll(input);
        final Object[] actuals = sut.elements().toArray();
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testWithNonComparableData() {
        final Bag<UncomparableType> sut = TreeBag.newTreeBag(createUncomparableTypeComparator());
        final UncomparableType[] input = createUncomparableTypes("b", "c", "a");
        final UncomparableType[] expecteds = createUncomparableTypes("a", "b", "c");
        sut.addAll(input);
        final Object[] actuals = sut.elements().toArray();
        assertArrayEquals(expecteds, actuals);
    }

    private Comparator<UncomparableType> createUncomparableTypeComparator() {
        return new Comparator<UncomparableType>() {
            @Override
            public int compare(final UncomparableType o1, final UncomparableType o2) {
                return o1.comparableValue.compareTo(o2.comparableValue);
            }
        };
    }

    private UncomparableType[] createUncomparableTypes(final String... values) {
        final UncomparableType[] result = new UncomparableType[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new UncomparableType();
            result[i].comparableValue = values[i];
        }
        return result;
    }

    private static class UncomparableType {
        private String comparableValue;

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }
}
