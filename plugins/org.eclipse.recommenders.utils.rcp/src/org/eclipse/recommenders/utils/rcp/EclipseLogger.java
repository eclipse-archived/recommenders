/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.rcp;

import static java.lang.String.format;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin.log;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.helpers.MarkerIgnoringBase;

public class EclipseLogger extends MarkerIgnoringBase {

    private static final long serialVersionUID = 1L;

    public void error(String format, Object arg) {
        IStatus newStatus = newStatus(ERROR, format, arg);
        log(newStatus);
    }

    private IStatus newStatus(int status, String format, Object... arg) {
        return newStatus(status, null, format, arg);
    }

    private IStatus newStatus(int status, Throwable t, String format, Object... arg) {
        String message = format(format == null ? "" : format, arg);
        IStatus newStatus = LoggingUtils.newStatus(status, t, "org.eclipse.recommenders.rcp.utils", message);
        return newStatus;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        IStatus newStatus = newStatus(INFO, msg);
        log(newStatus);
    }

    @Override
    public void trace(String format, Object arg) {
        IStatus newStatus = newStatus(INFO, format, arg);
        log(newStatus);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        IStatus newStatus = newStatus(INFO, format, arg1, arg2);
        log(newStatus);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        IStatus newStatus = newStatus(INFO, format, argArray);
        log(newStatus);
    }

    @Override
    public void trace(String msg, Throwable t) {
        IStatus newStatus = newStatus(INFO, t, null);
        log(newStatus);
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {
        IStatus newStatus = newStatus(INFO, msg);
        log(newStatus);
    }

    @Override
    public void debug(String format, Object arg) {
        IStatus newStatus = newStatus(INFO, format, arg);
        log(newStatus);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        IStatus newStatus = newStatus(INFO, format, arg1, arg2);
        log(newStatus);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        IStatus newStatus = newStatus(INFO, format, argArray);
        log(newStatus);
    }

    @Override
    public void debug(String msg, Throwable t) {
        IStatus newStatus = newStatus(INFO, t, msg);
        log(newStatus);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        IStatus newStatus = newStatus(INFO, msg);
        log(newStatus);
    }

    @Override
    public void info(String format, Object arg) {
        IStatus newStatus = newStatus(INFO, format, arg);
        log(newStatus);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        IStatus newStatus = newStatus(INFO, format, arg1, arg2);
        log(newStatus);
    }

    @Override
    public void info(String format, Object[] argArray) {
        IStatus newStatus = newStatus(INFO, format, argArray);
        log(newStatus);
    }

    @Override
    public void info(String msg, Throwable t) {
        IStatus newStatus = newStatus(INFO, t, msg);
        log(newStatus);
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
        IStatus newStatus = newStatus(WARNING, msg);
        log(newStatus);
    }

    @Override
    public void warn(String format, Object arg) {
        IStatus newStatus = newStatus(WARNING, format, arg);
        log(newStatus);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        IStatus newStatus = newStatus(WARNING, format, argArray);
        log(newStatus);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        IStatus newStatus = newStatus(WARNING, format, arg1, arg2);
        log(newStatus);
    }

    @Override
    public void warn(String msg, Throwable t) {
        IStatus newStatus = newStatus(WARNING, t, msg);
        log(newStatus);
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        IStatus newStatus = newStatus(ERROR, msg);
        log(newStatus);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        IStatus newStatus = newStatus(ERROR, format, arg1, arg2);
        log(newStatus);
    }

    @Override
    public void error(String format, Object[] argArray) {
        IStatus newStatus = newStatus(ERROR, format, argArray);
        log(newStatus);
    }

    @Override
    public void error(String msg, Throwable t) {
        IStatus newStatus = newStatus(ERROR, t, msg);
        log(newStatus);
    };
}
