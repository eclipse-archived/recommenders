/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.events;

import java.io.File;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;

public class NewClasspathEntryFound {

    public final IPackageFragmentRoot fragmentRoot;
    public final File fragmentRootLocation;
    public final ClasspathEntryInfo cpeInfo;

    public NewClasspathEntryFound(IPackageFragmentRoot fragmentRoot, File fragmentRootLocation,
            ClasspathEntryInfo cpeInfo) {
        this.fragmentRoot = fragmentRoot;
        this.fragmentRootLocation = fragmentRootLocation;
        this.cpeInfo = cpeInfo;
    }
}
