/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class MultipleDownloadCallbackTest {

    private static final int ONE_HUNDRED_PERCENT_UNITS = 100;
    private static final int MAXIMUM_DOWNLOADS = 2;

    private static final String MESSAGE = "Resolving model jre:jre:ovrp:zip:1.0.0";

    private static final Double REMAINDER_SPLIT = 1.0;

    private static final String MAVEN_METADATA_XML = "org/eclipse/recommenders/index/0.0.0-SNAPSHOT/maven-metadata.xml";
    private static final String INDEX_ZIP = "org/eclipse/recommenders/index/0.0.0-SNAPSHOT/index-0.0.0-20140605.014212-1.zip";
    private static final String JRE_ZIP = "jre/jre/1.0.0-SNAPSHOT/jre-1.0.0-20140605.013426-1-call.zip";

    @Mock
    private IProgressMonitor monitor;

    private ArgumentCaptor<Double> workedUnits;

    @Before
    public void setUp() {
        // Using @Captor would not work on kepler, which uses mockito 1.8.4
        // Mockito 1.8.4 creates an ArgumentCaptor<Object> which is not able to handle the autoboxing
        // of captured primitives (double in this case).
        // Details at: https://code.google.com/p/mockito/issues/detail?id=188
        // For that reason the captor is manually created.
        workedUnits = ArgumentCaptor.forClass(Double.class);

        // The callback uses a submonitor for downloads which will not call worked() but internalWorked() on the
        // callback's monitor.
        // Tests are only interested in the overall work units and not if they are passed by worked() or
        // internalWorked() to the monitor. For that reason the mock-object forwards all worked()-calls with the same
        // parameter to the internalWorked()-method (as most of the Monitor-Implementations do).
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                monitor.internalWorked((Integer) invocation.getArguments()[0]);
                return invocation;
            }
        }).when(monitor).worked(anyInt());
    }

    @Test
    public void testStartMonitorTask() {
        new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS, MAXIMUM_DOWNLOADS);

        verify(monitor).beginTask(eq(MESSAGE), eq(ONE_HUNDRED_PERCENT_UNITS));
    }

    @Test
    public void testMonitorNotDoneByDefault() {
        new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS, MAXIMUM_DOWNLOADS);

        verify(monitor, never()).done();
    }

    @Test
    public void testDownloadsFinishedDoesNotSetTheMonitorDone() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);

        sut.finish();

        verify(monitor, never()).done();
    }

    @Test
    public void testAllWorkDoneIfFinished() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);

        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, skipped(50.0), skipped(50.0));
    }

    @Test
    public void testAllWorkDoneIfFinishedWithOneDownload() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, download(50.0), skipped(50.0));
    }

    @Test
    public void testFinishWithoutDownloadIsNoSuccess() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);

        sut.finish();

        assertFalse(sut.isDownloadSucceeded());
    }

    @Test
    public void testDownloadSucceeded() {
        int maximumNumberOfDownloads = 1;
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                maximumNumberOfDownloads);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.finish();

        assertTrue(sut.isDownloadSucceeded());
    }

    @Test
    public void testSucceededWithOneFailedDownload() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.downloadInitiated(INDEX_ZIP);
        sut.downloadStarted(INDEX_ZIP);
        sut.downloadFailed(INDEX_ZIP);
        sut.finish();

        assertTrue(sut.isDownloadSucceeded());
    }

    @Test
    public void testNoDetailedProgressIfDownloadSizeNotKnown() {
        int numberOfDownloads = 1;
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                numberOfDownloads);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadProgressed(MAVEN_METADATA_XML, 512, -1);
        sut.downloadProgressed(MAVEN_METADATA_XML, 1024, -1);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, download(100.0));
    }

    @Test
    public void testFinishWithAllDownloads() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.downloadInitiated(INDEX_ZIP);
        sut.downloadStarted(INDEX_ZIP);
        sut.downloadSucceeded(INDEX_ZIP);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, download(50.0), download(50.0));
    }

    @Test
    public void testFinishedWithRemainderInWorkUnits() {
        int oneHundredPercentUnits = 13;
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, oneHundredPercentUnits,
                MAXIMUM_DOWNLOADS);

        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, download(6.0), download(6.0), REMAINDER_SPLIT);
    }

    @Test
    public void testDownloadProgressIfSizeKnown() {
        int maximumNumberOfDownloads = 1;
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                maximumNumberOfDownloads);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadProgressed(MAVEN_METADATA_XML, 256, 1024);
        sut.downloadProgressed(MAVEN_METADATA_XML, 512, 1024);
        sut.downloadProgressed(MAVEN_METADATA_XML, 1024, 1024);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, progress(25.0), progress(25.0), progress(50.0));
    }

    @Test
    public void testDownloadProgressWithMultipleDownloads() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);
        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadProgressed(MAVEN_METADATA_XML, 256, 1024);
        sut.downloadProgressed(MAVEN_METADATA_XML, 512, 1024);
        sut.downloadProgressed(MAVEN_METADATA_XML, 1024, 1024);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.downloadInitiated(INDEX_ZIP);
        sut.downloadStarted(INDEX_ZIP);
        sut.downloadSucceeded(INDEX_ZIP);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, progress(12.0), progress(12.0), progress(25.0), REMAINDER_SPLIT,
                download(50.0));
    }

    @Test
    public void testFinishedWorkWithFailedDownload() {
        int maximumNumberOfDownloads = 1;
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                maximumNumberOfDownloads);

        sut.downloadInitiated(JRE_ZIP);
        sut.downloadStarted(JRE_ZIP);
        sut.downloadFailed(JRE_ZIP);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, failed(100.0));
    }

    @Test
    public void testWorkUnitRemainderSplit() {
        int oneHundredPercentUnits = 17;
        int maximumNumberOfDownloads = 3;
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, oneHundredPercentUnits,
                maximumNumberOfDownloads);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.downloadInitiated(INDEX_ZIP);
        sut.downloadStarted(INDEX_ZIP);
        sut.downloadSucceeded(INDEX_ZIP);
        sut.downloadInitiated(JRE_ZIP);
        sut.downloadStarted(JRE_ZIP);
        sut.downloadSucceeded(JRE_ZIP);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, download(5.0), REMAINDER_SPLIT, download(5.0), REMAINDER_SPLIT,
                download(5.0));
    }

    @Test
    public void testMaximumDownloadsFinishWork() {
        MultipleDownloadCallback sut = new MultipleDownloadCallback(monitor, MESSAGE, ONE_HUNDRED_PERCENT_UNITS,
                MAXIMUM_DOWNLOADS);

        sut.downloadInitiated(MAVEN_METADATA_XML);
        sut.downloadStarted(MAVEN_METADATA_XML);
        sut.downloadSucceeded(MAVEN_METADATA_XML);
        sut.downloadInitiated(INDEX_ZIP);
        sut.downloadStarted(INDEX_ZIP);
        sut.downloadSucceeded(INDEX_ZIP);
        sut.finish();

        verify(monitor, atLeastOnce()).internalWorked(workedUnits.capture());
        assertCapturedSequence(workedUnits, download(50.0), download(50.0));
    }

    private static void assertCapturedSequence(ArgumentCaptor<Double> captor, Double... expectedValues) {
        List<Double> allValues = captor.getAllValues();
        assertThat(allValues, Matchers.equalTo(Arrays.asList(expectedValues)));
    }

    private static Double download(Double expectedValue) {
        return expectedValue;
    }

    private static Double progress(Double expectedValue) {
        return expectedValue;
    }

    private static Double skipped(Double expectedValue) {
        return expectedValue;
    }

    private static Double failed(Double expectedValue) {
        return expectedValue;
    }

}
