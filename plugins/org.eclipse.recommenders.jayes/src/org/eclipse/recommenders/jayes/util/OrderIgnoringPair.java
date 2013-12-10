/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.jayes.util;

public class OrderIgnoringPair<T> {

    private final int hashcode;

    private final T o1, o2;

    public OrderIgnoringPair(final T o1, final T o2) {
        this.o1 = o1;
        this.o2 = o2;
        hashcode = Math.min(o1.hashCode(), o2.hashCode()) + 67 * Math.max(o1.hashCode(), o2.hashCode());
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof OrderIgnoringPair)) {
            return false;
        }
        final OrderIgnoringPair<?> other = (OrderIgnoringPair<?>) o;
        return (bothNullOrEqual(getFirst(), other.getFirst()) && bothNullOrEqual(getSecond(), other.getSecond()))
                || equalsReverse(other);
    }

    private boolean equalsReverse(final OrderIgnoringPair<?> other) {
        return bothNullOrEqual(getSecond(), other.getFirst()) && bothNullOrEqual(getFirst(), other.getSecond());
    }

    private boolean bothNullOrEqual(T second, Object first) {
        return second == null ? first == null : second.equals(first);
    }

    public T getFirst() {
        return o1;
    }

    public T getSecond() {
        return o2;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }
}
