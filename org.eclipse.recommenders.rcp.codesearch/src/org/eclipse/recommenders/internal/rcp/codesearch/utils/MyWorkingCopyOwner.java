/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.utils;

import org.eclipse.jdt.core.WorkingCopyOwner;

public class MyWorkingCopyOwner extends WorkingCopyOwner {
    @Override
    public String findSource(final String ITypeName, final String packageName) {
        final String source = super.findSource(ITypeName, packageName);
        if (source == null || source.isEmpty()) {
            // System.out.println(ITypeName);
        }
        return source;
    }
}
