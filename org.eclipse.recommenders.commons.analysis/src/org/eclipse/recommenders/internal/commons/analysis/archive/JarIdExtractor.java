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
package org.eclipse.recommenders.internal.commons.analysis.archive;

import java.util.jar.JarFile;

import org.eclipse.recommenders.commons.utils.Version;

public abstract class JarIdExtractor implements IExtractor {

    private String name;
    private Version version = Version.UNKNOWN;

    @Override
    public abstract void extract(JarFile jarFile) throws Exception;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(final Version version) {
        this.version = version;
    }

}
