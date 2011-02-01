/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.utils;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class VersionedModuleName {

    public String name;

    public Version version;

    public Set<String> aliases;

    public static VersionedModuleName create(String name, Version version) {
        VersionedModuleName res = new VersionedModuleName();
        res.name = name;
        res.version = version;
        return res;
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }
}
