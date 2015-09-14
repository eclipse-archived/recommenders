/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static java.util.Objects.requireNonNull;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;

import java.util.Objects;

@SuppressWarnings("unchecked")
public abstract class Result<T> {

    public static final int OK = 0;
    public static final int ABSENT = -1;

    private static final Absent DEFAULT_ABSENT = new Absent(ABSENT, null);

    public static <T> Result<T> of(T reference) {
        return new Present<T>(requireNonNull(reference));
    }

    public static <T> Result<T> fromNullable(@Nullable T nullableReference) {
        return nullableReference == null ? Result.<T>absent() : new Present<T>(nullableReference);
    }

    public static <T> Result<T> absent() {
        return (Result<T>) DEFAULT_ABSENT;
    }

    public static <T> Result<T> absent(int code) {
        return (Result<T>) new Absent(code, null);
    }

    public static <T> Result<T> absent(Throwable exception) {
        return (Result<T>) new Absent(ABSENT, exception);
    }

    public static <T> Result<T> absent(int code, Throwable exception) {
        return (Result<T>) new Absent(code, exception);
    }

    public abstract boolean isPresent();

    public abstract boolean hasReason();

    public abstract int getReason();

    public abstract T get();

    public abstract T or(T defaultValue);

    public abstract Result<Throwable> getException();

    @Override
    public abstract boolean equals(@Nullable Object other);

    @Override
    public abstract int hashCode();

    private static final class Present<T> extends Result<T> {

        private final T reference;

        private Present(T reference) {
            this.reference = reference;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public boolean hasReason() {
            return false;
        }

        @Override
        public T or(T defaultValue) {
            return reference;
        }

        @Override
        public T get() {
            return reference;
        }

        @Override
        public int getReason() {
            return OK;
        }

        @Override
        public Result<Throwable> getException() {
            return Result.absent();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            Present<?> that = (Present<?>) other;
            return Objects.equals(this.reference, that.reference);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reference);
        }

        @Override
        public String toString() {
            return "Result.of(" + reference + ")";
        }
    }

    private static final class Absent extends Result<Object> {

        private final int reason;
        private final Throwable exception;

        private Absent(int reason, @Nullable Throwable exception) {
            ensureIsTrue(reason != OK);
            this.reason = reason;
            this.exception = exception;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public boolean hasReason() {
            return reason != ABSENT;
        }

        @Override
        public Object or(Object defaultValue) {
            return defaultValue;
        }

        @Override
        public Object get() {
            throw Throws.throwIllegalStateException("cannot get() value from Absent");
        }

        @Override
        public int getReason() {
            return reason;
        }

        @Override
        public Result<Throwable> getException() {
            return Result.fromNullable(exception);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            Absent that = (Absent) other;
            return this.reason == that.reason && Objects.equals(this.exception, that.exception);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reason, exception);
        }

        @Override
        public String toString() {
            return "Result.absent(" + reason + ", " + exception + ")";
        }
    }
}
