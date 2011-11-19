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

import static java.lang.String.format;

public class Tuple<T0, T1> {

    private T0 t0;

    private T1 t1;

    public static <T0, S0 extends T0, T1, S1 extends T1> Tuple<T0, T1> newTuple(final S0 t0, final S1 t1) {
        return new Tuple<T0, T1>(t0, t1);
    }

    @Deprecated
    public static <T0, S0 extends T0, T1, S1 extends T1> Tuple<T0, T1> create(final S0 t0, final S1 t1) {
        return newTuple(t0, t1);
    }

    protected Tuple() {
        // Used for deserialization
    }

    protected Tuple(final T0 t0, final T1 t1) {
        this.t0 = t0;
        this.t1 = t1;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Tuple) {
            final Tuple<?, ?> test = (Tuple<?, ?>) obj;
            return safeEquals(getFirst(), test.getFirst()) && safeEquals(getSecond(), test.getSecond());
        }
        return false;
    }

    private boolean safeEquals(final Object arg0, final Object arg1) {
        return arg0 == null ? arg1 == null : arg0.equals(arg1);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final int index) {
        return (T) (index == 0 ? t0 : t1);
    }

    public T0 getFirst() {
        return t0;
    }

    public T1 getSecond() {
        return t1;
    }

    @Override
    public int hashCode() {
        final int h0 = t0 == null ? 0 : t0.hashCode();
        final int h1 = t1 == null ? 0 : t1.hashCode();
        return h0 + h1;
    };

    @Override
    public String toString() {
        return format("<%s, %s>", getFirst(), getSecond());
    }
}
