package org.eclipse.recommenders.internal.rcp.analysis.cp;

import java.io.File;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.commons.utils.Fingerprints;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ClasspathEntry;

public class FingerprintClasspathEntryAnalyzer implements IClasspathEntryAnalyzer {

    @Override
    public void analyze(final IClasspathEntry jdtEntry, final ClasspathEntry recEntry) {
        final File location = recEntry.location;
        final String sha1 = Fingerprints.sha1(location);
        recEntry.fingerprint = sha1;
    }
}
