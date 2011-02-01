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
package org.eclipse.recommenders.commons.utils.names;

import java.util.Map;

import com.google.common.collect.MapMaker;

public class VmPackageName implements IPackageName {

    private static Map<String/* name 2 */, VmPackageName> index = new MapMaker().weakValues().makeMap();
    public static IPackageName DEFAULT_PACKAGE = get("");

    public static VmPackageName get(final String vmPackageName) {
        VmPackageName res = index.get(vmPackageName);
        if (res == null) {
            res = new VmPackageName(vmPackageName);
            index.put(vmPackageName, res);
        }
        return res;
    }

    private final String identifier;

    /**
     * @see #get(String)
     */
    private VmPackageName(final String vmPackageName) {
        identifier = vmPackageName;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public int compareTo(final IName o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public String toString() {
        return getIdentifier();
    }
}
