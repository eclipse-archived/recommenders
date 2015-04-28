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

import java.util.Objects;

@SuppressWarnings("unchecked")
public abstract class Result<T> {

    public static <T> Result<T> of(T reference) {
        return new Present<T>(requireNonNull(reference));
    }

    public static <T> Result<T> fromNullable(@Nullable T nullableReference) {
        return (nullableReference == null) ? Result.<T>absent() : new Present<T>(nullableReference);
    }

    public static <T> Result<T> absent() {
        return (Result<T>) Absent.INSTANCE;
    }

    public static <T> Result<T> error(int code) {
        return (Result<T>) new Error(code, null);
    }

    public static <T> Result<T> error(Throwable exception) {
        return (Result<T>) new Error(0, exception);
    }

    public static <T> Result<T> error(int code, Throwable exception) {
        return (Result<T>) new Error(code, exception);
    }

    public abstract boolean isPresent();

    public abstract boolean isError();

    public abstract T or(T defaultValue);

    public abstract T get();

    public abstract int getErrorCode();

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
        public boolean isError() {
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
        public int getErrorCode() {
            return 0;
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
        public boolean isError() {
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
        public int getErrorCode() {
            return 0;
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

    private static final class Error extends Result<Object> {

        private final int code;
        private final Throwable exception;

        private Error(int code, Throwable exception) {
            this.code = code;
            this.exception = exception;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public Object or(Object defaultValue) {
            return defaultValue;
        }

        @Override
        public Object get() {
            throw Throws.throwIllegalStateException("cannot get() value from Error");
        }

        @Override
        public int getErrorCode() {
            return code;
        }

        @Override
        public Result<Throwable> getException() {
            return Result.fromNullable(exception);
        }

        @Override
        public String toString() {
            return "Result.error(" + code + ", " + exception + ")";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other instanceof Error) {
                Error that = (Error) other;
                return code == that.code && Objects.equals(exception, that.exception);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 0x598df91c + code + (exception != null ? exception.hashCode() : 0);
        }
    }
}
