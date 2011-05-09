/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.server.stacktraces.crawler;

import java.util.LinkedList;
import java.util.List;

public class Stacktrace {

    public String exceptionType;
    public String message;
    public String uri;
    public List<StackFrame> frames = new LinkedList<StackFrame>();

    public Stacktrace() {

    }

    public Stacktrace(final String exceptionType, final String message, final List<StackFrame> frames) {
        this.exceptionType = exceptionType;
        this.message = message;
        this.frames = frames;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(exceptionType);
        if (message != null) {
            builder.append(": ");
            builder.append(message);
        }
        for (final StackFrame frame : frames) {
            builder.append("\n\tat ");
            builder.append(frame.toString());
        }

        return builder.toString();
    }
}
