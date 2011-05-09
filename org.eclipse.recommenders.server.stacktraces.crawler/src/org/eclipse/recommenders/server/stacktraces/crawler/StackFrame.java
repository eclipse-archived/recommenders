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


public class StackFrame {

    public String className;
    public String method;
    public String source;
    public String filename;
    public int lineNumber;

    public StackFrame() {

    }

    public StackFrame(final String className, final String method) {
        this.className = className;
        this.method = method;
    }

    public StackFrame(final String className, final String method, final String source, final String filename,
            final int lineNumber) {
        this.className = className;
        this.method = method;
        this.source = source;
        this.filename = filename;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return className + "." + method;
    }
}
