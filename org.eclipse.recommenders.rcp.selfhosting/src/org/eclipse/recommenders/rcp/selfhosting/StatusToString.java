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
package org.eclipse.recommenders.rcp.selfhosting;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

import org.eclipse.core.runtime.IStatus;

public class StatusToString {
    public static String toString(final IStatus status) {
        final StringBuilder sb = new StringBuilder();
        appendSeverityAndMessage(status, sb);
        appendException(status, sb);
        if (status.isMultiStatus()) {
            appendChildren(status, sb);
        }
        return sb.toString();
    }

    private static void appendSeverityAndMessage(final IStatus status, final StringBuilder sb) {
        sb.append(toSeverity(status)).append(": ").append(status.getMessage());
    }

    private static String toSeverity(final IStatus status) {
        switch (status.getSeverity()) {
        case IStatus.CANCEL:
            return "CANCEL";
        case IStatus.ERROR:
            return "ERROR";
        case IStatus.WARNING:
            return "WARN";
        case IStatus.INFO:
            return "INFO";
        case IStatus.OK:
            return "OK";
        default:
            throw throwUnreachable();
        }
    }

    private static void appendException(final IStatus status, final StringBuilder sb) {
        if (status.getException() != null) {
            sb.append(" ").append(status.getException());
        }
    }

    private static void appendChildren(final IStatus status, final StringBuilder sb) {
        for (final IStatus child : status.getChildren()) {
            sb.append("\n").append(toString(child));
        }
    }
}
