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
package org.eclipse.recommenders.utils.names;

import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.utils.annotations.Testing;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

public class VmPackageName implements IPackageName {

    private static Map<String/* name 2 */, VmPackageName> index = new MapMaker().weakValues().makeMap();
    public static IPackageName DEFAULT_PACKAGE = get("");

    public static synchronized VmPackageName get(final String vmPackageName) {
        VmPackageName res = index.get(vmPackageName);
        if (res == null) {
            res = new VmPackageName(vmPackageName);
            index.put(vmPackageName, res);
        }
        return res;
    }

    /**
     * @return the packages of the given types as returned by {@link ITypeName#getPackage()}
     */
    public static Set<IPackageName> packages(Set<ITypeName> types) {
        Set<IPackageName> res = Sets.newTreeSet();
        for (ITypeName type : types) {
            res.add(type.getPackage());
        }
        return res;
    }

    private final String identifier;

    /**
     * @see #get(String)
     */
    @Testing("Outside of tests, VmPackageNames should be canonicalized through VmPackageName#get(String)")
    protected VmPackageName(final String vmPackageName) {
        identifier = vmPackageName;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isDefaultPackage() {
        return getIdentifier().isEmpty();
    }

    public int compareTo(final IPackageName o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

}
