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

import java.util.concurrent.CancellationException;

public class Throws {

    public static IllegalArgumentException throwIllegalArgumentException(final String message) {
        throw new IllegalArgumentException(message);
    }

    public static IllegalArgumentException throwIllegalArgumentException(final String message, final Object... args) {
        final String formattedMessage = format(message, args);
        throw new IllegalArgumentException(formattedMessage);
    }

    public static IllegalStateException throwIllegalStateException(final String message) {
        throw new IllegalStateException(message);
    }

    public static IllegalStateException throwIllegalStateException(final String message, final Object... args) {
        final String formattedMessage = format(message, args);
        throw new IllegalStateException(formattedMessage);
    }

    public static IllegalStateException throwNotImplemented() {
        throw new IllegalStateException("not implemented");
    }

    public static IllegalStateException throwNotImplemented(final String message, final Object... args) {
        final String formattedMessage = format(message, args);
        throw new IllegalStateException(formattedMessage);
    }

    public static RuntimeException throwUnhandledException(final Exception cause) {
        throw new RuntimeException(cause);
    }

    public static RuntimeException throwUnhandledException(final Exception cause, final String msg,
            final Object... args) {
        final String formattedMessage = format(msg, args);
        throw new RuntimeException(formattedMessage, cause);
    }

    public static IllegalStateException throwUnreachable() {
        throw new IllegalStateException("reached code that should never get executed.");
    }

    public static IllegalStateException throwUnreachable(final String message, final Object... args) {
        final String formattedMessage = format(message, args);
        throw new IllegalStateException(formattedMessage);
    }

    public static UnsupportedOperationException throwUnsupportedOperation() {
        return throwUnsupportedOperation("This operation is not supported yet.");
    }

    public static UnsupportedOperationException throwUnsupportedOperation(final String message, final Object... args) {
        final String formattedMessage = format(message, args);
        throw new UnsupportedOperationException(formattedMessage);
    }

    public static CancellationException throwCancelationException() {
        throw new CancellationException();
    }

    public static CancellationException throwCancelationException(String message, Object... args) {
        final String formattedMessage = format(message, args);
        throw new CancellationException(formattedMessage);
    }
}
