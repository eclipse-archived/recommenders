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
package org.eclipse.recommenders.internal.calls.rcp.store;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.google.inject.assistedinject.Assisted;

public class ResolveCallsModelJob extends WorkspaceJob {

    private final File file;
    private final CallsModelResolver resolverService;
    private final CallsModelResolver.OverridePolicy overridePolicy;

    @Inject
    public ResolveCallsModelJob(@Assisted final File file,
            @Assisted final CallsModelResolver.OverridePolicy overridePolicy,
            final CallsModelResolver resolverService) {
        super(file.getName());
        this.file = file;
        this.overridePolicy = overridePolicy;
        this.resolverService = resolverService;
        setRule(new PackageRootSchedulingRule());
        setPriority(WorkspaceJob.DECORATE);
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        resolverService.resolve(file, overridePolicy);
        monitor.done();
        return Status.OK_STATUS;
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
