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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ThrowsTest {

    Exception expectedException = new Exception("message");
    String expectedMessage = "msg";

    @Test(expected = IllegalArgumentException.class)
    public void testThrowIllegalArgumentException() {
        Throws.throwIllegalArgumentException("");

    }

    @Test(expected = IllegalStateException.class)
    public void testThrowNotImplemented() {
        Throws.throwNotImplemented();
    }

    @Test
    public void testThrowUnhandledExceptionException() {
        try {
            Throws.throwUnhandledException(expectedException);
            fail();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            assertEquals(expectedException, cause);
        }
    }

    @Test
    public void testThrowUnhandledExceptionStringException() {
        try {
            Throws.throwUnhandledException(expectedException, expectedMessage);
            fail();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            assertEquals(expectedException, cause);
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowUnreachable() {
        Throws.throwUnreachable();
    }

    @Test
    public void testThrowUnreachableString() {
        try {
            Throws.throwUnreachable(expectedMessage);
            fail();
        } catch (Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testThrowUnsupportedOperation() {
        Throws.throwUnsupportedOperation();
    }

}
