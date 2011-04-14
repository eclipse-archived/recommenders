/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.utils;

import java.util.HashMap;
import java.util.Set;

public class BidirectionalHashMap<K, V> {

    private final HashMap<K, V> keyToValue;
    private final HashMap<V, K> valueToKey;

    public BidirectionalHashMap() {
        this.keyToValue = new HashMap<K, V>();
        this.valueToKey = new HashMap<V, K>();
    }

    public void put(final K key, final V value) {
        keyToValue.put(key, value);
        valueToKey.put(value, key);
    }

    public boolean containsValue(final V value) {
        return valueToKey.containsKey(value);
    }

    public Set<K> keySet() {
        return keyToValue.keySet();
    }

    public Set<V> valueSet() {
        return valueToKey.keySet();
    }

    public V getValue(final K key) {
        return keyToValue.get(key);
    }

    public K getKey(final V value) {
        return valueToKey.get(value);
    }
}
