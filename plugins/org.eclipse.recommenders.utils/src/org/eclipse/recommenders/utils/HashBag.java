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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

public class HashBag<T> implements Bag<T> {

    public static <T> HashBag<T> newHashBag() {
        return new HashBag<T>();
    }

    public static <T, S extends T> Bag<T> create(final Bag<S> bagToCopy) {
        final Bag<T> copy = new HashBag<T>();
        for (final T key : bagToCopy.elements()) {
            copy.add(key, bagToCopy.count(key));
        }
        return copy;
    }

    public static <T, S extends T> Bag<T> newHashBag(final Collection<S> elements) {
        final Bag<T> res = newHashBag();
        res.addAll(elements);
        return res;
    }

    protected Map<T, Integer> index = new HashMap<T, Integer>();

    protected HashBag() {
        //
    }

    public int absElementsCount() {
        int res = 0;
        for (final T t : index.keySet()) {
            res += count(t);
        }
        return res;
    }

    @Override
    public void add(final T obj) {
        add(obj, 1);
    }

    @Override
    public void add(final T key, final int frequency) {
        final Integer curFrequency = index.get(key);
        if (curFrequency == null) {
            index.put(key, new Integer(frequency));
        } else {
            index.put(key, curFrequency + frequency);
        }
    }

    @Override
    public void addAll(final Collection<? extends T> col) {
        addAll(col, 1);
    }

    @Override
    public void addAll(final Collection<? extends T> col, final int count) {
        for (final T elem : col) {
            add(elem, count);
        }
    }

    @Override
    public void addAll(final Map<? extends T, Integer> col) {
        for (final Entry<? extends T, Integer> pair : col.entrySet()) {
            add(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public void addAll(final T... elements) {
        for (final T element : elements) {
            add(element, 1);
        }
    }

    @Override
    public void addAll(final Bag<? extends T> bag) {
        for (final T element : bag) {
            add(element, bag.count(element));
        }
    }

    @Override
    public int count(final Object element) {
        final Integer count = index.get(element);
        return count == null ? 0 : count;
    }

    @Override
    public Set<T> elements() {
        return new HashSet<T>(index.keySet());
    }

    @Override
    public int elementsCount() {
        return index.size();
    }

    /**
     * @return a key set with keys sorted by frequency in ascending order. Note: This list is not backed up by the bag
     *         nor are changes to this list propagated to the bag.
     */
    public List<T> elementsOrderedByFrequency() {
        final ArrayList<T> res = new ArrayList<T>(index.keySet());
        Collections.sort(res, new Comparator<T>() {

            @Override
            public int compare(final T o1, final T o2) {
                return count(o2) - count(o1);
            }
        });
        return res;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Bag<?>)) {
            return false;
        }
        final Bag<?> other = (Bag<?>) obj;
        final Set<?> otherElements = other.elements();
        final Set<?> thisElements = elements();
        if (!otherElements.equals(thisElements)) {
            return false;
        }
        for (final Object element : thisElements) {
            final int thisCount = count(element);
            final int otherCount = other.count(element);
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

    @Override
    public Iterator<T> iterator() {
        return index.keySet().iterator();
    }

    @Override
    public void remove(final T key, final int frequency) {
        final Integer curFrequency = index.get(key);
        if (curFrequency != null) {
            if (curFrequency == frequency) {
                index.remove(key);
            } else {
                index.put(key, curFrequency - frequency);
            }
        }
    }

    @Override
    public void remove(final Bag<? extends T> bag) {
        for (final T element : bag) {
            remove(element, bag.count(element));
        }
    }

    @Override
    public void removeAll(final T element) {
        index.remove(element);
    }

    @Override
    public List<T> topElements(final int numberOfMaxTopElements) {
        final List<T> sortedKeys = elementsOrderedByFrequency();
        final int minElementsCount = Math.min(numberOfMaxTopElements, sortedKeys.size());
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

    @Override
    public int totalElementsCount() {
        int res = 0;
        for (final T t : index.keySet()) {
            res += count(t);
        }
        return res;
    }

    @Override
    public boolean contains(final T element) {
        return index.containsKey(element);
    }

    @Override
    public Map<T, Integer> asMap() {
        return Maps.newHashMap(index);
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

}
