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

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

public class StackTraceEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final StandardToStringStyle TO_STRING_STYLE;

    static {
        TO_STRING_STYLE = new StandardToStringStyle();
        TO_STRING_STYLE.setFieldSeparator("\n");
        TO_STRING_STYLE.setUseClassName(false);
        TO_STRING_STYLE.setUseIdentityHashCode(false);
        TO_STRING_STYLE.setArrayStart("");
        TO_STRING_STYLE.setArraySeparator("\n");
        TO_STRING_STYLE.setArrayEnd("");
        TO_STRING_STYLE.setContentStart("");
        TO_STRING_STYLE.setContentEnd("");
    }

    public UUID anonymousId;
    public UUID eventId;
    public UUID parentId;

    public String name;
    public String email;
    public String comment;

    public String pluginId;
    public String pluginVersion;

    public String eclipseBuildId;
    public String javaRuntimeVersion;

    public String osgiWs;
    public String osgiOs;
    public String osgiOsVersion;
    public String osgiArch;

    public Severity severity;
    public int code;
    public String message;

    public ThrowableDto[] trace;

    @Override
    public String toString() {

        ReflectionToStringBuilder toStringBuilder = new ReflectionToStringBuilder(this, TO_STRING_STYLE);
        toStringBuilder.setExcludeFieldNames("trace");
        String string = toStringBuilder.toString() + "\n";
        string += "trace= " + new ReflectionToStringBuilder(trace, TO_STRING_STYLE).toString();

        return string;
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
