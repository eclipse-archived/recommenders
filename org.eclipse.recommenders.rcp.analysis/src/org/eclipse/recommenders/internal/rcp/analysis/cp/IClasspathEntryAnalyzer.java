package org.eclipse.recommenders.internal.rcp.analysis.cp;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ClasspathEntry;

public interface IClasspathEntryAnalyzer {

    void analyze(IClasspathEntry jdtEntry, ClasspathEntry recEntry);

}
