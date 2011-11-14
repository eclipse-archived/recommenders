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

public interface Bag<T> extends Iterable<T> {
    /**
     * Adds the given element to this bag and increases its counter by one.
     */
    public abstract void add(final T element);

    /**
     * Adds the given elements to this bag and increases the counter to
     * count(key)+frequency.
     */
    public abstract void add(final T element, final int count);

    /**
     * Adds all given elements one-by-one to this bag.
     */
    public abstract void addAll(final Collection<? extends T> elements);

    /**
     * Adds all given elements to this bag with the given frequency.
     */
    public abstract void addAll(final Collection<? extends T> elements, final int frequency);

    /**
     * Adds all given elements one-by-one to this bag.
     */
    public abstract void addAll(final T... elements);

    public abstract void addAll(Bag<? extends T> bag);

    /**
     * @return the count how often the given object was added to this bag before
     */
    public abstract int count(final Object elements);

    /**
     * Returns an iterator over the unique set of instances in this bag.
     */
    @Override
    public abstract Iterator<T> iterator();

    /**
     * @return a copy of the bag's (distinct) set of elements, i.e., a set
     *         without any duplicates. Note, changes to this key set are not
     *         propagates to the bag.
     */
    public abstract Set<T> elements();

    /**
     * @return the number of distinct elements stored in this bag
     */
    public abstract int elementsCount();

    /**
     * Removes one element multiple times from the bag. The multiplicity is
     * given by parameter frequency. If the element count is 0 after removing
     * the element the Bag itself will not contain element anymore.
     */
    public abstract void remove(final T element, final int frequency);

    /**
     * Clears the given element completely from the bag (it does not decrease
     * the counter by one).
     */
    public abstract void removeAll(final T element);

    public abstract void remove(Bag<? extends T> bag);

    /**
     * @return the total number of elements stored in this bag. This sums up the
     *         frequency of each unique object stored in the key set of this
     *         bag.
     * 
     * @see #elementsCount()
     */
    public abstract int totalElementsCount();

    /**
     * @return the top-k most frequent elements in the bag.
     */
    public abstract List<T> topElements(int numberOfMostFrequentItems);

    /**
     * Returns true if the given element is already stored in this bag.
     */
    public abstract boolean contains(T element);

    /**
     * Returns the bag's content in a new map. Changes to this map are not
     * reflected by this bag.
     */
    public Map<T, Integer> asMap();

    public boolean isEmpty();
}
