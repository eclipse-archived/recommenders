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

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Throws.throwIllegalArgumentException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Triple<T0, T1, T2> {
    private final int FIRST = 0;
    private final int SECOND = 1;
    private final int THRID = 2;
    private final T0 t0;

    private final T1 t1;

    private final T2 t2;

    public static <T0, S0 extends T0, T1, S1 extends T1, T2, S2 extends T2> Triple<T0, T1, T2> create(final S0 t0,
            final S1 t1, final S2 t2) {
        return new Triple<T0, T1, T2>(t0, t1, t2);
    }

    protected Triple(final T0 t0, final T1 t1, final T2 t2) {
        this.t0 = t0;
        this.t1 = t1;
        this.t2 = t2;
    }

    public T0 getFirst() {
        return get(FIRST);
    }

    public T1 getSecond() {
        return get(SECOND);
    }

    public T2 getThird() {
        return get(THRID);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final int index) {
        switch (index) {
        case FIRST:
            return (T) t0;
        case SECOND:
            return (T) t1;
        case THRID:
            return (T) t2;
        default:
            throw throwIllegalArgumentException("index '%d' is not allowed!", index);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    };

    @Override
    public String toString() {
        return format("<%s, %s, %s>", getFirst(), getSecond(), getThird());
    }

}
