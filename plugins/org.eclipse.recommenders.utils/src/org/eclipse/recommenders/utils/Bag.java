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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Deprecated use Google Guava's {@code Multiset} classes instead.
 * @See Bags
 */
@Deprecated
public interface Bag<T> extends Iterable<T> {
    /**
     * Adds the given element to this bag and increases its counter by one.
     */
    void add(final T element);

    /**
     * Adds the given elements to this bag and increases the counter to count(key)+frequency.
     */
    void add(final T element, final int count);

    /**
     * Adds all given elements one-by-one to this bag.
     */
    void addAll(final Collection<? extends T> elements);

    /**
     * Adds all given elements to this bag with the given frequency.
     */
    void addAll(final Collection<? extends T> elements, final int frequency);

    /**
     * Adds all given elements one-by-one to this bag.
     */
    void addAll(final T... elements);

    void addAll(Bag<? extends T> bag);

    /**
     * Adds all given elements to this bag with the frequency given as values in the map.
     */
    void addAll(Map<? extends T, Integer> col);

    /**
     * @return the count how often the given object was added to this bag before
     */
    int count(final Object elements);

    /**
     * Returns an iterator over the unique set of instances in this bag.
     */
    @Override
    Iterator<T> iterator();

    /**
     * @return a copy of the bag's (distinct) set of elements, i.e., a set without any duplicates. Note, changes to this
     *         key set are not propagates to the bag.
     */
    Set<T> elements();

    /**
     * @return the number of distinct elements stored in this bag
     */
    int elementsCount();

    /**
     * Removes one element multiple times from the bag. The multiplicity is given by parameter frequency. If the element
     * count is 0 after removing the element the Bag itself will not contain element anymore.
     */
    void remove(final T element, final int frequency);

    /**
     * Clears the given element completely from the bag (it does not decrease the counter by one).
     */
    void removeAll(final T element);

    void remove(Bag<? extends T> bag);

    /**
     * @return the total number of elements stored in this bag. This sums up the frequency of each unique object stored
     *         in the key set of this bag.
     * 
     * @see #elementsCount()
     */
    int totalElementsCount();

    /**
     * @return the top-k most frequent elements in the bag.
     */
    List<T> topElements(int numberOfMostFrequentItems);

    /**
     * Returns true if the given element is already stored in this bag.
     */
    boolean contains(T element);

    /**
     * Returns the bag's content in a new map. Changes to this map are not reflected by this bag.
     */
    Map<T, Integer> asMap();

    boolean isEmpty();
}
