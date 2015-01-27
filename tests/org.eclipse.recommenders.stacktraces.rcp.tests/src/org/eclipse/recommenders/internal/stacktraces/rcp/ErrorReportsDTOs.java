/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Haftstein - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports.newErrorReport;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelFactory;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.StackTraceElement;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable;

public class ErrorReportsDTOs {
    private static final ModelFactory FACTORY = ModelFactory.eINSTANCE;

    public static java.lang.StackTraceElement[] createStacktraceForClasses(String... declaringClasses) {
        java.lang.StackTraceElement[] stackTraceElements = new java.lang.StackTraceElement[declaringClasses.length];
        for (int i = 0; i < declaringClasses.length; i++) {
            stackTraceElements[i] = createStackTraceElement(declaringClasses[i]);
        }
        return stackTraceElements;
    }

    private static java.lang.StackTraceElement createStackTraceElement(String declaringClass) {
        return new java.lang.StackTraceElement(declaringClass, "anyMethod", "Classname.java", -1);
    }

    public static StackTraceElement createStackTraceElement(String className, String methodName) {
        StackTraceElement element = ModelFactory.eINSTANCE.createStackTraceElement();
        element.setClassName(className);
        element.setMethodName(methodName);
        element.setFileName("file.java");
        return element;
    }

    public static Status createStatus(int severity, String pluginId, String message) {
        return createStatus(severity, pluginId, message, null);
    }

    public static Status createStatus(int severity, String pluginId, String message, java.lang.Throwable exception) {
        Status status = FACTORY.createStatus();
        status.setSeverity(severity);
        status.setPluginId(pluginId);
        status.setMessage(message);
        if (exception != null) {
            status.setException(ErrorReports.newThrowable(exception));
        }
        return status;
    }

    public static ErrorReport createTestReport() {
        RuntimeException cause = new RuntimeException("cause");
        Exception exception = new RuntimeException("exception message", cause);
        exception.fillInStackTrace();
        IStatus status = new org.eclipse.core.runtime.Status(IStatus.ERROR, "org.eclipse.recommenders.stacktraces",
                "some error message", exception);

        Settings settings = ModelFactory.eINSTANCE.createSettings();
        settings.setWhitelistedPackages(newArrayList("org."));

        return newErrorReport(status, settings);
    }

    public static Throwable createThrowable(String className) {
        Throwable throwable = ModelFactory.eINSTANCE.createThrowable();
        throwable.setClassName(className);
        return throwable;
    }
}
