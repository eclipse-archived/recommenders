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

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class StackTraceEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public UUID anonymousId;
    public String name;
    public String email;

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

    public ThrowableDto[] chain;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
