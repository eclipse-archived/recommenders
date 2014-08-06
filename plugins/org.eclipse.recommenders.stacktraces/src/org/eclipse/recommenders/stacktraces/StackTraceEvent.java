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
package org.eclipse.recommenders.stacktraces;

import java.io.Serializable;
import java.util.Map;

public class StackTraceEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public String email;
    public String name;

    public String pluginId;
    public int severity;
    public int code;
    public String message;

    public ThrowableDto exception;

    public Map<String, String> properties;

    public String pluginVersion;
}
