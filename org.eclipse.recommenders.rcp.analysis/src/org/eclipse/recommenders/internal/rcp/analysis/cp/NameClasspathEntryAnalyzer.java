package org.eclipse.recommenders.internal.rcp.analysis.cp;

import java.util.Set;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ClasspathEntry;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class NameClasspathEntryAnalyzer implements IClasspathEntryAnalyzer {

    private final Provider<Set<INameFinder>> provider;

    @Inject
    public NameClasspathEntryAnalyzer(final Provider<Set<INameFinder>> provider) {
        this.provider = provider;
    }

    @Override
    public void analyze(final IClasspathEntry jdtEntry, final ClasspathEntry recEntry) {
        for (final INameFinder finder : provider.get()) {
            try {
                final String name = finder.find(recEntry.location);
                if (name != INameFinder.UNKNOWN) {
                    recEntry.name = name;
                    return;
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        setNameToUnknown(recEntry);
    }

    private void setNameToUnknown(final ClasspathEntry recEntry) {
        recEntry.name = INameFinder.UNKNOWN;
    }
}
