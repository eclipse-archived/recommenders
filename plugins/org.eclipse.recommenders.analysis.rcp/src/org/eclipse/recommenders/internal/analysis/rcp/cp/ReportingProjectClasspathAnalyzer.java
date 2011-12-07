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

import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.io.File;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.analysis.ClasspathEntry;
import org.eclipse.recommenders.internal.analysis.ProjectClasspath;
import org.eclipse.recommenders.internal.analysis.rcp.ClasspathEntryStore;
import org.eclipse.recommenders.utils.Fingerprints;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ReportingProjectClasspathAnalyzer implements IProjectClasspathAnalyzer {

    private final ClasspathEntryStore cpStore;
    private final Provider<Set<IClasspathEntryAnalyzer>> provider;

    @Inject
    public ReportingProjectClasspathAnalyzer(final ClasspathEntryStore cpStore,
            final Provider<Set<IClasspathEntryAnalyzer>> provider) {
        this.cpStore = cpStore;
        this.provider = provider;
    }

    @Override
    public ProjectClasspath analyze(final IJavaProject project, final IProgressMonitor monitor) {
        final ProjectClasspath cp = ProjectClasspath.create();
        monitor.beginTask("Code Recommenders: Analyzing Classpath...", 1);
        try {
            for (final IClasspathEntry entry : project.getResolvedClasspath(true)) {
                switch (entry.getEntryKind()) {
                case IClasspathEntry.CPE_LIBRARY:
                    monitor.subTask("Recommenders: Scanning " + entry.getPath().lastSegment());
                    inspectLibrary(entry, cp);
                    monitor.worked(1);
                    break;
                default:
                    break;
                }
            }
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
        return cp;
    }

    private void inspectLibrary(final IClasspathEntry jdtEntry, final ProjectClasspath recClasspath) {
        final File file = jdtEntry.getPath().toFile();
        final String sha1 = Fingerprints.sha1(file);

        if (cpStore.hasModel(sha1)) {
            final ClasspathEntry entry = cpStore.get(sha1);
            recClasspath.path.add(entry);
            return;
        }

        final ClasspathEntry recEntry = new ClasspathEntry();
        for (final IClasspathEntryAnalyzer analyzer : provider.get()) {
            analyzer.analyze(jdtEntry, recEntry);
        }
        recClasspath.path.add(recEntry);
        cpStore.register(recEntry);
    }
}
