/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;

public class Bags {

    private static final Comparator<Entry<?>> BY_COUNT = new Comparator<Multiset.Entry<?>>() {
        @Override
        public int compare(Entry<?> o1, Entry<?> o2) {
            return ComparisonChain.start().compare(o1.getCount(), o2.getCount())
                    .compare(o1.getElement().toString(), o2.getElement().toString()).result();
        }
    };

    private static final Comparator<Entry<?>> BY_STRING = new Comparator<Multiset.Entry<?>>() {
        @Override
        public int compare(Entry<?> o1, Entry<?> o2) {
            return ComparisonChain.start().compare(o1.getElement().toString(), o2.getElement().toString())
                    .compare(o1.getCount(), o2.getCount()).result();
        }
    };

    public static <T> List<Entry<T>> topUsingCount(Multiset<T> set, int i) {
        Set<Entry<T>> entries = set.entrySet();
        return Ordering.from(BY_COUNT).greatestOf(entries, i);
    }

    public static <T> List<Entry<T>> topUsingToString(Multiset<T> set, int i) {
        Set<Entry<T>> entries = set.entrySet();
        return Ordering.from(BY_STRING).greatestOf(entries, i);
    }

    public static <T> List<Entry<T>> orderedByToString(Multiset<T> set) {
        Set<Entry<T>> entries = set.entrySet();
        return Ordering.from(BY_STRING).sortedCopy(entries);
    }

    public static <T> List<Entry<T>> orderedByCount(Multiset<T> set) {
        Set<Entry<T>> entries = set.entrySet();
        return Ordering.from(BY_COUNT).sortedCopy(entries);
    }

}
