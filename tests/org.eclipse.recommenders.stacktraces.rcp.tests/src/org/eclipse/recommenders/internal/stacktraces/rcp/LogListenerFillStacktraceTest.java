/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class LogListenerFillStacktraceTest {

    @Test
    public void test() {
        // setup:
        Status empty = new Status(IStatus.ERROR, "debug", "has no stacktrace");
        Assert.assertThat(empty.getException(), CoreMatchers.nullValue());
        // exercise:
        LogListener.insertDebugStacktraceIfEmpty(empty);
        // verify:
        assertThat(empty.getException(), notNullValue());
        assertTrue(empty.getException().getStackTrace().length > 0);
    }

}
