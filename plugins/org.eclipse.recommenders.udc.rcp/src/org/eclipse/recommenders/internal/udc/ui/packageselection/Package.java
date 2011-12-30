/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.packageselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;

public class Package {
    List<IFolder> relatedFolders = new ArrayList<IFolder>();
    String packageName;
    String lazyPackageIdentifier;
    Package parentPackage;

    public Package(final String packageName, final Package parentPackage) {
        super();
        this.packageName = packageName;
        this.parentPackage = parentPackage;
    }

    public List<IFolder> getRelatedFolders() {
        return relatedFolders;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageIdentifier() {
        if (lazyPackageIdentifier == null) {
            initPackageIdentifier();
        }
        return lazyPackageIdentifier;
    }

    private void initPackageIdentifier() {
        if (parentPackage == null || parentPackage.getPackageName().isEmpty()) {
            lazyPackageIdentifier = getPackageName();
        } else {
            lazyPackageIdentifier = parentPackage.getPackageIdentifier() + "." + getPackageName();
        }
    }

    @Override
    public String toString() {
        return getPackageName();
    }
}