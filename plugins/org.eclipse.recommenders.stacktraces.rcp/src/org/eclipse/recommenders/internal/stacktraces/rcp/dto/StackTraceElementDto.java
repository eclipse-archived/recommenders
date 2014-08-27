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
package org.eclipse.recommenders.internal.stacktraces.rcp.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class StackTraceElementDto {

    public static StackTraceElementDto from(StackTraceElement e) {
        StackTraceElementDto res = new StackTraceElementDto();
        res.classname = e.getClassName();
        res.methodname = e.getMethodName();
        res.line = e.getLineNumber();
        res.isNative = e.isNativeMethod();
        res.filename = e.getFileName();
        return res;
    }

    public String classname;
    public String methodname;
    public String filename;
    public int line;
    public boolean isNative;

    @Override
    public String toString() {
        return classname + "." + methodname + "(" + filename + ":" + line + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
