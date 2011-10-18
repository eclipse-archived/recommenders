/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import static java.lang.String.format;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.utils.Option;

import com.google.inject.assistedinject.Assisted;

public class ResolveCallsModelJob extends WorkspaceJob {

    public static enum OverridePolicy {
        NONE, ALL, MANIFEST
    }

    private final File file;
    private IProgressMonitor monitor;
    private final CallsModelResolverService resolverService;
    private final OverridePolicy overridePolicy;
    private final ClasspathDependencyStore dependencyStore;
    private Option<ClasspathDependencyInformation> dependencyInfo = Option.none();

    @Inject
    public ResolveCallsModelJob(@Assisted final File file, @Assisted final OverridePolicy overridePolicy,
            final CallsModelResolverService resolverService, final ClasspathDependencyStore dependencyStore) {
        super(file.getName());
        this.file = file;
        this.overridePolicy = overridePolicy;
        this.resolverService = resolverService;
        this.dependencyStore = dependencyStore;
        setRule(new PackageRootSchedulingRule());
        setPriority(WorkspaceJob.DECORATE);
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        this.monitor = monitor;
        resolve();
        return Status.OK_STATUS;
    }

    private void resolve() {
        monitor.beginTask(format("Requesting calls model for '%s'.", file.getPath()), 100);
        findClasspathDependencyInformation();
        monitor.worked(10);
        findModel();
        monitor.done();
    }

    private void findClasspathDependencyInformation() {
        if (overridePolicy != OverridePolicy.ALL && dependencyStore.containsClasspathDependencyInfo(file)) {
            dependencyInfo = Option.wrap(dependencyStore.getClasspathDependencyInfo(file));
        } else {
            monitor.subTask("Extracting classpath dependecy information...");
            dependencyInfo = resolverService.tryExtractClasspathDependencyInfo(file);
            if (dependencyInfo.hasValue()) {
                dependencyStore.putClasspathDependencyInfo(file, dependencyInfo.get());
            }
        }
    }

    private void findModel() {
        if (!dependencyInfo.hasValue()) {
            return;
        }
        if (overridePolicy == OverridePolicy.NONE && resolverService.hasModel(file)) {
            return;
        }
        resolverService.downloadAndRegisterModel(file, dependencyInfo.get());
    }

    private class PackageRootSchedulingRule implements ISchedulingRule {

        @Override
        public boolean contains(final ISchedulingRule rule) {
            return isConflicting(rule);
        }

        @Override
        public boolean isConflicting(final ISchedulingRule rule) {
            if (rule instanceof PackageRootSchedulingRule) {
                final File otherFile = ((PackageRootSchedulingRule) rule).getFile();
                return otherFile.equals(file);
            }
            return false;
        }

        private File getFile() {
            return file;
        }

    }
}
