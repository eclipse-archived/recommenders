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

import java.io.File;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.analysis.ClasspathEntry;
import org.eclipse.recommenders.utils.Fingerprints;

public class FingerprintClasspathEntryAnalyzer implements IClasspathEntryAnalyzer {

    @Override
    public void analyze(final IClasspathEntry jdtEntry, final ClasspathEntry recEntry) {
        final File location = recEntry.location;
        final String sha1 = Fingerprints.sha1(location);
        recEntry.fingerprint = sha1;
    }
}
