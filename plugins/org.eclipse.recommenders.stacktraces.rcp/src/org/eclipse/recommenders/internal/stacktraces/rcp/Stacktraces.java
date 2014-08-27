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

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Throwables.getCausalChain;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.internal.stacktraces.rcp.StacktraceWizard.WizardPreferences;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.Severity;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.StackTraceElementDto;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.StackTraceEvent;
import org.eclipse.recommenders.internal.stacktraces.rcp.dto.ThrowableDto;
import org.eclipse.recommenders.utils.AnonymousId;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

public class Stacktraces {

    private static final String ANONYMIZED_TAG = "HIDDEN";
    private static final List<String> PREFIX_WHITELIST = Arrays.asList("java.", "javax.", "org.eclipse.");

    public static final String PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp";

    public static StackTraceEvent createDto(IStatus status, StacktracesRcpPreferences stacktracesPreferences,
            WizardPreferences wizardPreferences) {
        StackTraceEvent event = createDto(status, stacktracesPreferences);
        event.name = wizardPreferences.name;
        event.email = wizardPreferences.email;
        if (wizardPreferences.clearMessages) {
            clearMessages(event);
        }
        if (wizardPreferences.anonymize) {
            anonymizeStackTraceElements(event);
        }
        return event;
    }

    public static StackTraceEvent createDto(IStatus status, StacktracesRcpPreferences pref) {
        StackTraceEvent event = new StackTraceEvent();
        event.anonymousId = AnonymousId.getId();
        event.eventId = UUID.randomUUID();
        event.name = pref.getName();
        event.email = pref.getEmail();
        event.severity = getSeverity(status);
        event.code = status.getCode();
        event.message = status.getMessage();
        event.pluginId = status.getPlugin();

        event.javaRuntimeVersion = SystemUtils.JAVA_RUNTIME_VERSION;
        event.eclipseBuildId = getProperty("eclipse.buildId", "-");
        event.osgiArch = getProperty("osgi.arch", "-");
        event.osgiWs = getProperty("osgi.ws", "-");
        event.osgiOs = getProperty(Constants.FRAMEWORK_OS_NAME, "-");
        event.osgiOsVersion = getProperty(Constants.FRAMEWORK_OS_VERSION, "-");

        Bundle bundle = Platform.getBundle(status.getPlugin());
        if (bundle != null) {
            event.pluginVersion = bundle.getVersion().toString();
        }

        if (status.getException() != null) {
            List<ThrowableDto> exs = newLinkedList();
            for (Throwable t : getCausalChain(status.getException())) {
                exs.add(ThrowableDto.from(t));
            }
            event.trace = toArray(exs, ThrowableDto.class);
        }

        if (pref.shouldClearMessages()) {
            clearMessages(event);
        }
        if (pref.shouldAnonymizeStackTraceElements()) {
            anonymizeStackTraceElements(event);
        }
        return event;
    }

    public static void clearMessages(StackTraceEvent event) {
        event.message = ANONYMIZED_TAG;
        for (ThrowableDto dto : event.trace) {
            dto.message = ANONYMIZED_TAG;
        }
    }

    public static void anonymizeStackTraceElements(StackTraceEvent event) {
        if (event.trace != null) {
            for (ThrowableDto dto : event.trace) {
                anonymizeStackTraceElements(dto);
            }
        }
    }

    static void anonymizeStackTraceElements(ThrowableDto dto) {
        if (!matchesWhitelist(dto.classname, PREFIX_WHITELIST)) {
            dto.classname = ANONYMIZED_TAG;
        }
        if (dto.elements != null) {
            for (StackTraceElementDto stDto : dto.elements) {
                anonymizeStackTraceElement(stDto);
            }
        }
    }

    static void anonymizeStackTraceElement(StackTraceElementDto stDto) {
        if (!matchesWhitelist(stDto.classname, PREFIX_WHITELIST)) {
            stDto.classname = ANONYMIZED_TAG;
            stDto.methodname = ANONYMIZED_TAG;
        }
    }

    private static boolean matchesWhitelist(String className, List<String> whitelist) {
        for (String whiteListedPrefix : whitelist) {
            if (className.startsWith(whiteListedPrefix)) {
                return true;
            }
        }
        return false;
    }

    private static Severity getSeverity(IStatus status) {
        switch (status.getSeverity()) {
        case IStatus.OK:
            return Severity.OK;
        case IStatus.CANCEL:
            return Severity.CANCEL;
        case IStatus.INFO:
            return Severity.INFO;
        case IStatus.ERROR:
            return Severity.ERROR;
        case IStatus.WARNING:
            return Severity.WARN;
        default:
            return Severity.UNKNOWN;
        }
    }

    private static String getProperty(String key, String defaultValue) {
        return firstNonNull(System.getProperty(key), defaultValue);
    }

}
