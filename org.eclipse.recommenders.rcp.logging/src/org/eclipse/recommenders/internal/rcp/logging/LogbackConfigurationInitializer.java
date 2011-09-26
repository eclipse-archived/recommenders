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
package org.eclipse.recommenders.internal.rcp.logging;

import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsDirectory;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsFile;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.internal.rcp.logging.LoggingActivator.BUNDLE_ID;
import static org.eclipse.recommenders.rcp.utils.LoggingUtils.newError;
import static org.eclipse.recommenders.rcp.utils.LoggingUtils.newInfo;
import static org.eclipse.recommenders.rcp.utils.LoggingUtils.newWarning;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class LogbackConfigurationInitializer implements Callable<IStatus> {
    protected static final String LOGBACK_BUNDLE_ID = "ch.qos.logback.core";
    protected static final String PROP_LOGBACK_CONFIGURATION_FILE = "logback.configurationFile";

    @Override
    public IStatus call() {
        if (isLogbackAlreadyLoaded()) {
            return newError(null, BUNDLE_ID,
                    "Logback has already started. Configuration file needs to be set manually.");
        } else if (isLogbackConfigurationFilePropertyDefined()) {
            return checkAndWarnConfigurationFileExists();
        } else {
            return setLogbackSystemPropertyToFallbackConfiguration();
        }
    }

    private IStatus checkAndWarnConfigurationFileExists() {
        final File file = new File(getLogbackProperty()).getAbsoluteFile();
        if (!file.isFile()) {
            return newWarning(null, BUNDLE_ID, "failed to initialize logback logging for code recommenders", file);
        }
        return OK_STATUS;
    }

    protected boolean isLogbackAlreadyLoaded() {
        final Bundle bundle = Platform.getBundle(LOGBACK_BUNDLE_ID);
        if (bundle == null) {
            return false;
        }
        return Bundle.ACTIVE == bundle.getState();
    }

    protected boolean isLogbackConfigurationFilePropertyDefined() {
        return getLogbackProperty() != null;
    }

    private String getLogbackProperty() {
        return System.getProperty(PROP_LOGBACK_CONFIGURATION_FILE);
    }

    private void setLogbackConfigurationFileProperty(final File f) {
        System.setProperty(PROP_LOGBACK_CONFIGURATION_FILE, f.getAbsolutePath());
    }

    private IStatus setLogbackSystemPropertyToFallbackConfiguration() {
        try {
            final File logbackConfigurationFile = getFallbackConfigurationFile();
            setLogbackConfigurationFileProperty(logbackConfigurationFile);
            return newInfo(BUNDLE_ID, "Temporarirly set logback configuration file to %s", logbackConfigurationFile);
        } catch (final Exception e) {
            return newError(e, BUNDLE_ID, "Failed to lookup fallback config file. Couldn't initialize logback");
        }
    }

    protected File getFallbackConfigurationFile() throws IOException, RuntimeException {
        final Bundle bundle = Platform.getBundle("org.eclipse.recommenders.server.setup");
        ensureIsNotNull(bundle);
        final File bundleFile = FileLocator.getBundleFile(bundle);
        ensureIsDirectory(bundleFile);
        final File logbackConfigurationFile = new File(bundleFile, "logback.xml");
        ensureIsFile(logbackConfigurationFile);
        return logbackConfigurationFile;
    }
}
