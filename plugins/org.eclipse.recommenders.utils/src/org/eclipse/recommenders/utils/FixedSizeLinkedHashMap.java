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

import java.util.LinkedHashMap;

public class FixedSizeLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;

    public static <K, V> FixedSizeLinkedHashMap<K, V> create(final int maxSize) {
        return new FixedSizeLinkedHashMap<K, V>(maxSize);
    }

    private final int maxSize;

    protected FixedSizeLinkedHashMap(final int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(final java.util.Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}