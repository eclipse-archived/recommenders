/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.commons.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

public abstract class Option<T> {

    public static <T> Option<T> wrap(final T value) {
        return value == null ? new None<T>() : new Some<T>(value);
    }

    public abstract boolean hasValue();

    public abstract T get();

    public T getOrElse(final T alternative) {
        return hasValue() ? get() : alternative;
    }

    public static final class Some<T> extends Option<T> {

        private final T value;

        public Some(final T value) {
            ensureIsNotNull(value);
            this.value = value;
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public String toString() {
            return "Some(" + value + ")";
        }

        @Override
        public boolean equals(final Object other) {
            if (other == null || other.getClass() != Some.class) {
                return false;
            }
            final Some<?> that = (Some<?>) other;
            final Object thatValue = that.get();
            return value.equals(thatValue);
        }

        @Override
        public int hashCode() {
            return 37 * value.hashCode();
        }
    }

    public static final class None<T> extends Option<T> {

        @Override
        public boolean hasValue() {
            return false;
        }

        @Override
        public T get() {
            throw throwUnreachable("None has no value.");
        }

        @Override
        public int hashCode() {
            return -1;
        }

        @Override
        public boolean equals(final Object other) {
            return other == null || other.getClass() != None.class ? false : true;
        }

        @Override
        public String toString() {
            return "None()";
        }
    }
}
