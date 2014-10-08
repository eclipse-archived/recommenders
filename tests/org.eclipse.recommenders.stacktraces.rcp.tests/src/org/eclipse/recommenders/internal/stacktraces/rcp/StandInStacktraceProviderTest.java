/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Haftstein - initial tests.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class StandInStacktraceProviderTest {

    private static final String ANY_CLASS_1 = "any.class.Classname1";
    private static final String ANY_CLASS_2 = "other.class.Classname2";
    private static final String ANY_CLASS_3 = "some.other.class.Classname3";

    private static final String BLACKLISTED_CLASS_1 = "known.package.Class1";
    private static final String BLACKLISTED_CLASS_2 = "other.known.package.Class2";
    private static final String BLACKLISTED_CLASS_3 = "some.known.package.Class3";

    private static final Set<String> BLACKLIST = Sets.newHashSet(BLACKLISTED_CLASS_1, BLACKLISTED_CLASS_2);

    @Spy
    private StandInStacktraceProvider stacktraceProvider = new StandInStacktraceProvider();

    private static StackTraceElement[] buildTraceForClasses(String... classnames) {
        StackTraceElement[] elements = new StackTraceElement[classnames.length];
        for (int i = 0; i < classnames.length; i++) {
            elements[i] = new StackTraceElement(classnames[i], "anyMethod", classnames[i] + ".java", -1);
        }
        return elements;
    }

    @Test
    public void testClearBlacklistedOnTop() {
        StackTraceElement[] stackframes = buildTraceForClasses(BLACKLISTED_CLASS_1, ANY_CLASS_1);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = buildTraceForClasses(ANY_CLASS_1);
        assertThat(cleared, is(expected));
    }

    @Test
    public void testClearMultipleBlacklistedOnTop() {
        StackTraceElement[] stackframes = buildTraceForClasses(BLACKLISTED_CLASS_1, BLACKLISTED_CLASS_2, ANY_CLASS_1);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = buildTraceForClasses(ANY_CLASS_1);
        assertThat(cleared, is(expected));
    }

    @Test
    public void testDoNotClearBlacklistedOnBottom() {
        StackTraceElement[] stackframes = buildTraceForClasses(ANY_CLASS_1, BLACKLISTED_CLASS_1);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = stackframes;
        assertThat(cleared, is(expected));
    }

    @Test
    public void testDoNotClearBlacklistedOnBottomButOnTop() {
        StackTraceElement[] stackframes = buildTraceForClasses(BLACKLISTED_CLASS_1, BLACKLISTED_CLASS_2, ANY_CLASS_1,
                BLACKLISTED_CLASS_3);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = buildTraceForClasses(ANY_CLASS_1, BLACKLISTED_CLASS_3);
        assertThat(cleared, is(expected));
    }

    @Test
    public void testDoNotClearUnknownClasses() {
        StackTraceElement[] stackframes = buildTraceForClasses(ANY_CLASS_1, ANY_CLASS_2, ANY_CLASS_3);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = stackframes;
        assertThat(cleared, is(expected));
    }

    @Test
    public void testInsertStacktraceForStatusWithNoException() {
        IStatus status = new Status(IStatus.ERROR, "plugin.id", "any message");
        stacktraceProvider.insertStandInStacktraceIfEmpty(status);
        Mockito.verify(stacktraceProvider).clearBlacklistedTopStackframes(Mockito.any(StackTraceElement[].class),
                Mockito.anySetOf(String.class));
    }

    @Test
    public void testInsertedExceptionClass() {
        IStatus status = new Status(IStatus.ERROR, "plugin.id", "any message");
        stacktraceProvider.insertStandInStacktraceIfEmpty(status);
        Assert.assertTrue(status.getException() instanceof StandInStacktraceProvider.StandInException);
    }

    @Test
    public void testInsertClearedStackframesInStatusWithNoException() {
        IStatus status = new Status(IStatus.ERROR, "plugin.id", "any message");
        stacktraceProvider.insertStandInStacktraceIfEmpty(status);
        ArgumentCaptor<StackTraceElement[]> captor = ArgumentCaptor.forClass(StackTraceElement[].class);
        Mockito.verify(stacktraceProvider).clearBlacklistedTopStackframes(captor.capture(),
                Mockito.anySetOf(String.class));
        assertThat(status.getException().getStackTrace(), is(captor.getValue()));
    }

    @Test
    public void testInsertStacktraceSkippedForStatusWithException() {
        IStatus status = new Status(IStatus.ERROR, "plugin.id", "any message", new RuntimeException());
        stacktraceProvider.insertStandInStacktraceIfEmpty(status);
        Mockito.verify(stacktraceProvider, never()).clearBlacklistedTopStackframes(
                Mockito.any(StackTraceElement[].class), Mockito.anySetOf(String.class));
    }

    @Test
    public void testInsertStacktraceSkippedForNoStatusInstance() {
        IStatus status = Mockito.mock(IStatus.class);
        stacktraceProvider.insertStandInStacktraceIfEmpty(status);
        Mockito.verify(stacktraceProvider, never()).clearBlacklistedTopStackframes(
                Mockito.any(StackTraceElement[].class), Mockito.anySetOf(String.class));
    }
}
