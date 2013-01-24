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
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class contains various frequently used checks we used in our code base. Some methods return their arguments
 * where appropriate, others return {@link Void} if a return value has no or at least ambiguous meaning.
 * 
 */
public class Checks {
    public static void ensureEquals(final Object value, final Object expected, final String message) {
        final boolean equals = value == null ? expected == null : value.equals(expected);
        if (!equals) {
            final String formattedMessage = format("Expected %s but got %s -- %s ", expected, value, message);
            throwIllegalArgumentException(formattedMessage);
        }
    }

    public static void ensureEquals(final Object value, final Object expected, final String message, Object... args) {
        final boolean equals = value == null ? expected == null : value.equals(expected);
        if (!equals) {
            final String error = format(message, args);
            final String formattedMessage = format("Expected %s but got %s -- %s ", expected, value, error);
            throwIllegalArgumentException(formattedMessage);
        }
    }

    public static void ensureSame(int actual, int expected, final String message, Object... args) {
        if (actual != expected) {
            final String formattedMessage = format("Expected %s but got %s -- %s ", expected, actual, message);
            throwIllegalArgumentException(formattedMessage);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object object) {
        return (T) object;
    }

    /**
     * @return the given file or raises an exception
     */
    public static File ensureExists(final File file) {
        ensureIsNotNull(file);
        if (!file.exists()) {
            try {
                file.getCanonicalFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            throwIllegalArgumentException("file %s does not exist.", file.getAbsolutePath());
        }
        return file;
    }

    /**
     * @return the given directory or raises an exception
     */
    public static File ensureIsDirectory(final File directory) {
        ensureIsNotNull(directory);
        if (!directory.isDirectory()) {
            throwIllegalArgumentException("file %s is not a directory.", directory.getAbsolutePath());
        }
        return directory;
    }

    public static <T extends Collection<?>> T ensureIsEmpty(final T collection) {
        if (!collection.isEmpty()) {
            throwIllegalArgumentException("collection is not empty!");
        }
        return collection;
    }

    /**
     * @return the given file or raises an exception if the file is not a file
     */
    public static File ensureIsFile(final File file) {
        ensureIsNotNull(file);
        if (file.isDirectory()) {
            throwIllegalArgumentException("file %s is a directory.", file.getAbsolutePath());
        }
        return file;
    }

    public static void ensureIsFalse(final boolean expression, final String message, final Object... args) {
        if (expression) {
            throwIllegalArgumentException(message, args);
        }
    }

    public static void ensureIsGreaterOrEqualTo(final double value, final double min, final String message) {
        if (value < min) {
            throwIllegalArgumentException("value '%f' is smaller than '%f': %s", value, min, message);
        }
    }

    public static void ensureIsGreaterOrEqualTo(final double value, final double min, final String message,
            final Object... args) {
        if (value < min) {
            throwIllegalArgumentException(message, args);
        }
    }

    public static double ensureIsProbability(final double value) {
        return ensureIsInRange(value, 0.0d, 1.0d, "value not in range [0,1]: %3.3f", value);
    }

    public static double ensureIsInRange(final double value, final double min, final double max, final String message,
            final Object... args) {
        final boolean isInRange = value >= min && value <= max;
        if (!isInRange) {
            throwIllegalArgumentException(message, args);
        }
        return value;
    }

    /**
     * @return the given object if it is an instance of the given class or raises an exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T ensureIsInstanceOf(final Object obj, final Class<T> clazz) {
        ensureIsNotNull(clazz);
        ensureIsNotNull(obj, "null is not an instance of type '%s'", clazz.getName());
        final boolean instanceOf = clazz.isInstance(obj);
        if (!instanceOf) {
            throwIllegalArgumentException("object of type '%s' is not a (sub-)type of '%s'", obj.getClass().getName(),
                    clazz.getName());
        }
        return (T) obj;
    }

    /**
     * @return the iterable or raises an exception if iterable contains no elements
     */
    public static <T extends Iterable<?>> T ensureIsNotEmpty(final T iterable, final String message,
            final Object... args) {
        ensureIsNotNull(iterable);
        final Iterator<?> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            throwIllegalArgumentException(message, args);
        }
        return iterable;
    }

    /**
     * @return the given testValue or raises an exception if this string is empty
     */
    public static String ensureIsNotEmpty(final String testValue, final String message, final Object... args) {
        ensureIsNotNull(testValue);
        if (testValue.length() == 0) {
            throwIllegalArgumentException(message, args);
        }
        return testValue;
    }

    public static void ensureIsNotInstanceOf(final Object type, final Class<?> expectedType) {
        ensureIsNotNull(expectedType);
        final boolean isInstanceOf = expectedType.isInstance(type);
        if (isInstanceOf) {
            throwIllegalArgumentException("Wrong type - %s is instanceof %s but not allowed", type, expectedType);
        }
    }

    /**
     * @see #ensureIsNotNull(Object, String)
     */
    public static <T> T ensureIsNotNull(final T arg) {
        return ensureIsNotNull(arg, "???");
    }

    public static <T> T ensureIsNotNull(final T arg, final String message, final Object... args) {
        if (arg == null) {
            throwIllegalArgumentException(message, args);
        }
        return arg;
    }

    /**
     * @return the given value or raises an exception if the value is '0'
     */
    public static int ensureIsNotZero(final int value) {
        if (value == 0) {
            throwIllegalArgumentException("Value of '0' is not allowed");
        }
        return value;
    }

    public static void ensureIsNull(final Object arg) {
        ensureIsNull(arg, "Expected object to be null but got:%s", arg);
    }

    public static void ensureIsNull(final Object arg, final String message, final Object... args) {
        if (arg != null) {
            throwIllegalArgumentException(message, args);
        }
    }

    /**
     * Checks whether the array of double values is sorted - but does not sort the array.
     */
    // TODO should we rename this method since this name may imply that it
    // sorts the Array?
    public static void ensureIsSorted(final double[] values) {
        for (int i = values.length; i-- > 1;) {
            if (values[i] > values[i - 1]) {
                throw new IllegalArgumentException("values are not sorted");
            }
        }
    }

    public static void ensureIsTrue(final boolean exp) {
        ensureIsTrue(exp, "assertion failed.");
    }

    public static void ensureIsTrue(final boolean exp, final String message, final Object... args) {
        if (!exp) {
            throwIllegalArgumentException(message, args);
        }
    }

    private Checks() {
        // no-one should instantiate this class
    }
}
