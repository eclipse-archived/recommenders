package org.eclipse.recommenders.internal.rcp.analysis.cp;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ClasspathEntry;

public class LocationClasspathEntryAnalyzer implements IClasspathEntryAnalyzer {

    @Override
    public void analyze(final IClasspathEntry jdtEntry, final ClasspathEntry recEntry) {
        recEntry.location = jdtEntry.getPath().toFile();
    }

}
