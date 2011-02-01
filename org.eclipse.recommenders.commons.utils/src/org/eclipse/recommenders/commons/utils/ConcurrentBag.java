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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;

public class ConcurrentBag<T> implements Bag<T> {

    public static <T> ConcurrentBag<T> newConcurrentBag() {
        return new ConcurrentBag<T>();
    }

    public static <T, S extends T> Bag<T> newConcurrentBag(final Bag<S> bagToCopy) {
        final Bag<T> copy = new ConcurrentBag<T>();
        for (final T key : bagToCopy.elements()) {
            copy.add(key, bagToCopy.count(key));
        }
        return copy;
    }

    public static <T, S extends T> Bag<T> newConcurrentBag(final Collection<S> elements) {
        final Bag<T> res = newConcurrentBag();
        res.addAll(elements);
        return res;
    }

    protected Map<T, AtomicInteger> index = Maps.newConcurrentMap();

    protected ConcurrentBag() {
        //
    }

    public int absElementsCount() {
        int res = 0;
        for (final T t : index.keySet()) {
            res += count(t);
        }
        return res;
    }

    public void add(final T obj) {
        add(obj, 1);
    }

    public void add(final T key, final int frequency) {
        final AtomicInteger curFrequency = index.get(key);
        if (curFrequency == null) {
            index.put(key, new AtomicInteger(frequency));
        } else {
            curFrequency.addAndGet(frequency);
        }
    }

    public void addAll(final Collection<? extends T> col) {
        addAll(col, 1);
    }

    public void addAll(final Collection<? extends T> col, final int count) {
        for (final T elem : col) {
            add(elem, count);
        }
    }

    public void addAll(final T... elements) {
        for (T element : elements) {
            add(element, 1);
        }
    }

    @Override
    public int count(final Object element) {
        final AtomicInteger count = index.get(element);
        return count == null ? 0 : count.get();
    }

    public Set<T> elements() {
        return new TreeSet<T>(index.keySet());
    }

    public int elementsCount() {
        return index.size();
    }

    /**
     * @return a key set with keys sorted by frequency in ascending order. Note:
     *         This list is not backed up by the bag nor are changes to this
     *         list propagated to the bag.
     */
    public List<T> elementsOrderedByFrequency() {
        final ArrayList<T> res = new ArrayList<T>(index.keySet());
        Collections.sort(res, new Comparator<T>() {

            public int compare(final T o1, final T o2) {
                return count(o2) - count(o1);
            }
        });
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Bag<?>)) {
            return false;
        }
        Bag<?> other = (Bag<?>) obj;
        Set<?> otherElements = other.elements();
        Set<?> thisElements = elements();
        if (!otherElements.equals(thisElements)) {
            return false;
        }
        for (Object element : thisElements) {
            int thisCount = count(element);
            int otherCount = other.count(element);
            if (thisCount != otherCount) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return index.hashCode();
    }

    public Iterator<T> iterator() {
        return index.keySet().iterator();
    }

    public void remove(final T element) {
        index.remove(element);
    }

    @Override
    public List<T> topElements(final int numberOfMaxTopElements) {
        final List<T> sortedKeys = elementsOrderedByFrequency();
        int minElementsCount = Math.min(numberOfMaxTopElements, sortedKeys.size());
        final List<T> topList = sortedKeys.subList(0, minElementsCount);
        return topList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(elementsCount()).append(" keys, ").append(totalElementsCount()).append(" total entries\n");
        sb.append(elementsCount()).append(" keys, ").append(absElementsCount()).append(" total entries\n");
        for (final T elem : index.keySet()) {
            sb.append(elem).append(" : ").append(index.get(elem)).append("\n");
        }
        return sb.toString();
    }

    public int totalElementsCount() {
        int res = 0;
        for (final T t : index.keySet()) {
            res += count(t);
        }
        return res;
    }

    @Override
    public boolean contains(T element) {
        return index.containsKey(element);
    }
}
