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

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.recommenders.internal.stacktraces.rcp.ErrorReportsDTOs.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelFactory;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Status;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private Settings settings;

    @Before
    public void setUp() {
        settings = ModelFactory.eINSTANCE.createSettings();
        settings.setWhitelistedPluginIds(newArrayList("plugin.id"));
        settings.setWhitelistedPackages(newArrayList("java"));
    }

    @Spy
    private StandInStacktraceProvider stacktraceProvider = new StandInStacktraceProvider();

    @Test
    public void testClearBlacklistedOnTop() {
        StackTraceElement[] stackframes = createStacktraceForClasses(BLACKLISTED_CLASS_1, ANY_CLASS_1);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = createStacktraceForClasses(ANY_CLASS_1);
        assertThat(cleared, is(expected));
    }

    @Test
    public void testClearMultipleBlacklistedOnTop() {
        StackTraceElement[] stackframes = createStacktraceForClasses(BLACKLISTED_CLASS_1, BLACKLISTED_CLASS_2,
                ANY_CLASS_1);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = createStacktraceForClasses(ANY_CLASS_1);
        assertThat(cleared, is(expected));
    }

    @Test
    public void testDoNotClearBlacklistedOnBottom() {
        StackTraceElement[] stackframes = createStacktraceForClasses(ANY_CLASS_1, BLACKLISTED_CLASS_1);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = stackframes;
        assertThat(cleared, is(expected));
    }

    @Test
    public void testDoNotClearBlacklistedOnBottomButOnTop() {
        StackTraceElement[] stackframes = createStacktraceForClasses(BLACKLISTED_CLASS_1, BLACKLISTED_CLASS_2,
                ANY_CLASS_1, BLACKLISTED_CLASS_3);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = createStacktraceForClasses(ANY_CLASS_1, BLACKLISTED_CLASS_3);
        assertThat(cleared, is(expected));
    }

    @Test
    public void testDoNotClearUnknownClasses() {
        StackTraceElement[] stackframes = createStacktraceForClasses(ANY_CLASS_1, ANY_CLASS_2, ANY_CLASS_3);

        StackTraceElement[] cleared = stacktraceProvider.clearBlacklistedTopStackframes(stackframes, BLACKLIST);

        StackTraceElement[] expected = stackframes;
        assertThat(cleared, is(expected));
    }

    @Test
    public void testInsertStacktraceForStatusWithNoException() {
        Status status = createStatus(IStatus.ERROR, "plugin.id", "any message");
        stacktraceProvider.insertStandInStacktraceIfEmpty(status, settings);
        Mockito.verify(stacktraceProvider).clearBlacklistedTopStackframes(Mockito.any(StackTraceElement[].class),
                Mockito.anySetOf(String.class));
    }

    @Test
    public void testInsertedExceptionClass() {
        Status status = createStatus(IStatus.ERROR, "plugin.id", "any message");
        stacktraceProvider.insertStandInStacktraceIfEmpty(status, settings);
        Assert.assertThat(status.getException().getClassName(),
                is(StandInStacktraceProvider.StandInException.class.getName()));
    }

    @Test
    public void testInsertStacktraceSkippedForStatusWithException() {
        Status status = createStatus(IStatus.ERROR, "plugin.id", "any message", new RuntimeException());
        stacktraceProvider.insertStandInStacktraceIfEmpty(status, settings);
        Mockito.verify(stacktraceProvider, never()).clearBlacklistedTopStackframes(
                Mockito.any(StackTraceElement[].class), Mockito.anySetOf(String.class));
    }

    @Test
    public void testInserterClassNotContainedInStacktrace() {
        Status status = createStatus(IStatus.ERROR, "plugin.id", "any message");
        new StandInStacktraceProvider().insertStandInStacktraceIfEmpty(status, settings);
        for (org.eclipse.recommenders.internal.stacktraces.rcp.model.StackTraceElement e : status.getException()
                .getStackTrace()) {
            assertThat(e.getClassName(), not(is(StandInStacktraceProvider.class.getCanonicalName())));
        }
    }

    @Test
    public void testFingerprintUpdated() {
        Status status = createStatus(IStatus.ERROR, "plugin.id", "any message");
        String oldFingerprint = status.getFingerprint();
        new StandInStacktraceProvider().insertStandInStacktraceIfEmpty(status, settings);
        assertThat(status.getFingerprint(), not(is(oldFingerprint)));
    }
}
