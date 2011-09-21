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

import java.util.TreeSet;

import com.google.common.collect.Maps;

public class TreeBag<T extends Comparable<T>> extends HashBag<T> {

    public static <E extends Comparable<E>> TreeBag<E> newTreeBag() {
        return new TreeBag<E>();
    }

    protected TreeBag() {
        super();
        index = Maps.newTreeMap();
    }

    @Override
    public TreeSet<T> elements() {
        return new TreeSet<T>(index.keySet());
    }
}
