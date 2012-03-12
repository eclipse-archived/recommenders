/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.internal.rcp.logging;

import static org.eclipse.recommenders.internal.rcp.logging.LogbackConfigurationInitializer.PROP_LOGBACK_CONFIGURATION_FILE;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.internal.rcp.logging.LogbackConfigurationInitializer;
import org.junit.Test;

public class LogbackConfigurationInitializerTest {

    private LogbackConfigurationInitializer sut;

    @Test
    public void testSetFallbackLogbackConfiguration() throws Exception {
        sut = new LogbackConfigurationInitializer() {
            @Override
            protected File getFallbackConfigurationFile() throws IOException, RuntimeException {
                return new File("/tmp");
            }
        };
        sut.call();
        assertEquals(new File("/tmp").getAbsolutePath(), System.getProperty(PROP_LOGBACK_CONFIGURATION_FILE));
    }

    @Test
    public void testDontTouchExistingConfigButWarnIfNotExists() throws Exception {
        final String expected = "some-value";
        System.setProperty(PROP_LOGBACK_CONFIGURATION_FILE, expected);
        sut = new LogbackConfigurationInitializer();
        final IStatus status = sut.call();
        assertEquals(IStatus.WARNING, status.getSeverity());
        final String actual = System.getProperty(PROP_LOGBACK_CONFIGURATION_FILE);
        assertEquals(expected, actual);
    }

    @Test
    public void testDontTouchExistingConfig() throws Exception {
        final File f = File.createTempFile("test", "txt");
        f.deleteOnExit();
        final String expected = f.getAbsolutePath();
        System.setProperty(PROP_LOGBACK_CONFIGURATION_FILE, expected);
        sut = new LogbackConfigurationInitializer();
        final IStatus status = sut.call();
        assertEquals(IStatus.OK, status.getSeverity());
        final String actual = System.getProperty(PROP_LOGBACK_CONFIGURATION_FILE);
        assertEquals(expected, actual);
    }
}
