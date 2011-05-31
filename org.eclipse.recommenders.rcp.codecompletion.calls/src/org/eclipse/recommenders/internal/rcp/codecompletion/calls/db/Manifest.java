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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.db;

import java.util.Date;

import org.eclipse.recommenders.commons.utils.Version;

public class Manifest {

    private String name;
    private Version version;
    private Date timestamp;

    public String getName() {
        return name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Version getVersion() {
        return version;
    }
}
