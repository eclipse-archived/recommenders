package org.eclipse.recommenders.commons.utils;

import static java.lang.String.format;

public class Triple<T0, T1, T2> {
    private final T0 t0;

    private final T1 t1;

    private final T2 t2;

    public static <T0, S0 extends T0, T1, S1 extends T1, T2, S2 extends T2> Triple<T0, T1, T2> create(final S0 t0,
            final S1 t1, final S2 t2) {
        return new Triple<T0, T1, T2>(t0, t1, t2);
    }

    protected Triple(final T0 t0, final T1 t1, T2 t2) {
        this.t0 = t0;
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Tuple) {
            final Triple<?, ?, ?> test = (Triple<?, ?, ?>) obj;
            return safeEquals(getFirst(), test.getFirst()) && safeEquals(getSecond(), test.getSecond())
                    && safeEquals(getThird(), test.getThird());
        }
        return false;
    }

    private boolean safeEquals(Object arg0, Object arg1) {
        return arg0 == null ? arg1 == null : arg0.equals(arg1);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final int index) {
        switch (index) {
        case 0:
            return (T) t0;
        case 1:
            return (T) t1;
        case 2:
            return (T) t2;
        }
        return null;
    }

    public T0 getFirst() {
        return t0;
    }

    public T1 getSecond() {
        return t1;
    }

    public T2 getThird() {
        return t2;
    }

    @Override
    public int hashCode() {
        int h0 = t0 == null ? 0 : t0.hashCode();
        int h1 = t1 == null ? 0 : t1.hashCode();
        int h2 = t2 == null ? 0 : t2.hashCode();
        return h0 + h1 + h2;
    };

    @Override
    public String toString() {
        return format("<%s, %s, %s>", getFirst(), getSecond(), getThird());
    }

}
