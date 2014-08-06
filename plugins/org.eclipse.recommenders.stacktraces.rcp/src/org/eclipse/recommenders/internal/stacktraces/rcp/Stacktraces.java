/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.stacktraces.StackTraceEvent;
import org.eclipse.recommenders.stacktraces.ThrowableDto;
import org.osgi.framework.Bundle;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class Stacktraces {

    public static final String PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp";

    public static StackTraceEvent createDto(IStatus status, StacktracesRcpPreferences pref) {
        StackTraceEvent event = new StackTraceEvent();
        event.name = pref.name;
        event.email = pref.email;
        event.severity = status.getSeverity();
        event.code = status.getCode();
        event.message = status.getMessage();
        event.pluginId = status.getPlugin();

        event.properties = Maps.newTreeMap();
        event.properties.put("java.runtime.version", SystemUtils.JAVA_RUNTIME_VERSION);
        event.properties.put("os.arch", SystemUtils.OS_ARCH);
        event.properties.put("os.arch", SystemUtils.OS_ARCH);
        event.properties.put("os.name", SystemUtils.OS_NAME);
        event.properties.put("os.version", SystemUtils.OS_VERSION);
        event.properties.put("eclipse.buildId", getProperty("eclipse.buildId", "-"));
        event.properties.put("eclipse.commands", getProperty("eclipse.commands", "-"));
        event.properties.put("osgi.arch", getProperty("osgi.arch", "-"));
        event.properties.put("osgi.os", getProperty("osgi.os", "-"));
        event.properties.put("osgi.ws", getProperty("osgi.ws", "-"));

        Bundle bundle = Platform.getBundle(status.getPlugin());
        if (bundle != null) {
            event.pluginVersion = bundle.getVersion().toString();
        }

        event.exception = ThrowableDto.from(status.getException());
        return event;
    }

    private static String getProperty(String key, String defaultValue) {
        return Objects.firstNonNull(System.getProperty(key), defaultValue);
    }

}
