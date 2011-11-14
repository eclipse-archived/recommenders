/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.analysis.rcp.cp;

import java.util.Set;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.analysis.analyzers.modules.ClasspathEntry;
import org.eclipse.recommenders.utils.Version;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VersionClasspathEntryAnalyzer implements IClasspathEntryAnalyzer {

    private final Provider<Set<IVersionFinder>> provider;

    @Inject
    public VersionClasspathEntryAnalyzer(final Provider<Set<IVersionFinder>> provider) {
        this.provider = provider;
    }

    @Override
    public void analyze(final IClasspathEntry jdtEntry, final ClasspathEntry recEntry) {
        for (final IVersionFinder finder : provider.get()) {
            try {
                final Version version = finder.find(recEntry.location);
                if (version != IVersionFinder.UNKNOWN) {
                    recEntry.version = version;
                    return;
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        setVersionToUnknown(recEntry);
    }

    private void setVersionToUnknown(final ClasspathEntry recEntry) {
        recEntry.version = IVersionFinder.UNKNOWN;
    }
}
