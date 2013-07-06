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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

@Deprecated
public class TreeBag<T> extends HashBag<T> {

    private Comparator<T> comparator;

    public static <E extends Comparable<E>> TreeBag<E> newTreeBag() {
        return new TreeBag<E>();
    }

    public static <E> TreeBag<E> newTreeBag(final Comparator<E> comparator) {
        return new TreeBag<E>(comparator);
    }

    public static <E extends Comparable<E>> TreeBag<E> newTreeBag(final Map<E, Integer> values) {
        final TreeBag<E> res = new TreeBag<E>();
        res.addAll(values);
        return res;
    }

    protected TreeBag() {
        index = new TreeMap<T, Integer>();
    }

    protected TreeBag(final Comparator<T> comparator) {
        this.comparator = comparator;
        index = new TreeMap<T, Integer>(comparator);
    }

    @Override
    public TreeSet<T> elements() {
        TreeSet<T> result;
        if (comparator == null) {
            result = new TreeSet<T>();
        } else {
            result = new TreeSet<T>(comparator);
        }
        result.addAll(index.keySet());
        return result;
    }
}
