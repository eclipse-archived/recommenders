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

    public static <T> Result<T> of(T reference) {
        return new Present<T>(requireNonNull(reference));
    }

    public static <T> Result<T> fromNullable(@Nullable T nullableReference) {
        return (nullableReference == null) ? Result.<T>absent() : new Present<T>(nullableReference);
    }

    public static <T> Result<T> absent() {
        return (Result<T>) Absent.INSTANCE;
    }

    public static <T> Result<T> absent(int code) {
        return (Result<T>) new AbsentWithReason(code, null);
    }

    public static <T> Result<T> absent(Throwable exception) {
        return (Result<T>) new AbsentWithReason(0, exception);
    }

    public static <T> Result<T> absent(int code, Throwable exception) {
        return (Result<T>) new AbsentWithReason(code, exception);
    }

    public abstract boolean isPresent();

    public abstract boolean hasReason();

    public abstract int getReason();

    public abstract T get();

    public abstract T or(T defaultValue);

    public abstract Result<Throwable> getException();

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
        public String toString() {
            return "Result.of(" + reference + ")";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other instanceof Present) {
                Present<?> that = (Present<?>) other;
                return reference.equals(that.reference);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 0x598df91c + reference.hashCode();
        }
    }

    private static final class Absent extends Result<Object> {

        private static final Absent INSTANCE = new Absent();

        private Absent() {
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public boolean hasReason() {
            return false;
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
            return ABSENT;
        }

        @Override
        public Result<Throwable> getException() {
            return Result.absent();
        }

        @Override
        public String toString() {
            return "Result.absent()";
        }

        @Override
        public boolean equals(@Nullable Object that) {
            return this == that;
        }

        @Override
        public int hashCode() {
            return 0x598df91c;
        }
    }

    private static final class AbsentWithReason extends Result<Object> {

        private final int reason;
        private final Throwable exception;

        private AbsentWithReason(int reason, Throwable exception) {
            ensureIsTrue(reason < ABSENT || reason > OK, "Reason must not be -1 or 0.");
            this.reason = reason;
            this.exception = exception;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public boolean hasReason() {
            return true;
        }

        @Override
        public Object or(Object defaultValue) {
            return defaultValue;
        }

        @Override
        public Object get() {
            throw Throws.throwIllegalStateException("cannot get() value from AbsentWithReason");
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
        public String toString() {
            return "Result.error(" + reason + ", " + exception + ")";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other instanceof AbsentWithReason) {
                AbsentWithReason that = (AbsentWithReason) other;
                return reason == that.reason && Objects.equals(exception, that.exception);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 0x598df91c + reason + (exception != null ? exception.hashCode() : 0);
        }
    }
}
