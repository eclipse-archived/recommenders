/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.rcp.providers.cpe;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.analysis.ClasspathEntry;

public class LocationClasspathEntryAnalyzer implements IClasspathEntryAnalyzer {

    @Override
    public void analyze(final IClasspathEntry jdtEntry, final ClasspathEntry recEntry) {
        recEntry.location = jdtEntry.getPath().toFile();
    }

}
