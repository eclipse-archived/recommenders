package org.eclipse.recommenders.internal.rcp.analysis.cp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ProjectClasspath;

public interface IProjectClasspathAnalyzer {

    public ProjectClasspath analyze(final IJavaProject project, final IProgressMonitor monitor);
}
