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
package org.eclipse.recommenders.utils.rcp.internal;

import org.eclipse.jdt.core.WorkingCopyOwner;

// XXX is this class actually needed?
public class MyWorkingCopyOwner extends WorkingCopyOwner {

    @Override
    public String findSource(final String typeName, final String packageName) {
        final String source = super.findSource(typeName, packageName);
        if (source == null || source.isEmpty()) {
        }
        return source;
    }
}
