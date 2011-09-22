/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.udc.depersonalizer.ICompilationUnitDepersonalizer;
import org.eclipse.recommenders.internal.udc.ui.packageselection.PackageTester;

public class CompilationUnitExportJob extends Job {
    private final class CompilationUnitCollector implements IResourceVisitor {
        private final List<IFile> result;

        private CompilationUnitCollector(final List<IFile> result) {
            this.result = result;
        }

        @Override
        public boolean visit(final IResource resource) throws CoreException {
            if (resource instanceof IFile) {
                final IFile file = (IFile) resource;
                if (file.getName().equals("CompilationUnit.json")) {
                    if (!isFileSizeBelowMax(file)) {
                        return true;
                    }
                    result.add(file);
                }
            }
            return true;
        }

        private boolean isFileSizeBelowMax(final IFile file) {
            final long fileSize = getFileSize(file);
            if (fileSize > maxFileSize) {
                Activator
                        .getDefault()
                        .getLog()
                        .log(new Status(Status.ERROR, Activator.PLUGIN_ID, "Can't export compilationunit "
                                + file.toString() + " : the file's size(+" + fileSize
                                + ") exceeds the max. file size of" + maxFileSize));
                return false;
            } else {
                return true;
            }

        }

        private long getFileSize(final IFile file) {
            return file.getRawLocation().toFile().length();
        }
    }

    private static final long maxFileSize = 3000000;

    private final IProject[] projects;
    private final ICompilationUnitDepersonalizer[] depersonalizers;
    private final ICompilationUnitExporter exporter;
    private final PackageTester packageTester;

    public CompilationUnitExportJob(final IProject[] projects, final ICompilationUnitDepersonalizer[] depersonalizers,
            final ICompilationUnitExporter exporter, final PackageTester packageTester) {
        super("CompilationUnit export");
        this.projects = projects;
        this.depersonalizers = depersonalizers;
        this.exporter = exporter;
        this.packageTester = packageTester;
    }

    public boolean isFileToBig(final IFile file) {
        return file.getRawLocation().toFile().length() > maxFileSize;
    }

    private List<IFile> collectFiles(final IProject project) {
        final IFolder dataFolder = project.getFolder(".recommenders/data");
        final List<IFile> result = new ArrayList<IFile>();
        if (dataFolder.exists()) {
            try {
                dataFolder.accept(new CompilationUnitCollector(result));
            } catch (final CoreException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        monitor.beginTask("Export Recommender Data", projects.length);
        try {
            for (final IProject project : projects) {
                ensureJobIsNotCanceled(monitor);
                exportProject(project, new SubProgressMonitor(monitor, 2));
            }
            exporter.done();
            updateLastUploadPreference();
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    private void updateLastUploadPreference() {
        UploadPreferences.setLastUploadDate(System.currentTimeMillis());
    }

    private void exportProject(final IProject project, final IProgressMonitor monitor) {
        monitor.beginTask("Export project " + project.getName(), 5);
        final List<IFile> collectedFiles = collectFiles(project);
        monitor.worked(1);
        monitor.subTask("Deserializing compilation units");
        final List<CompilationUnit> units = deserializeCompilationUnits(collectedFiles, new SubProgressMonitor(monitor,
                1));
        monitor.worked(1);
        monitor.subTask("Anonymizing upload data.");
        depersonalizeUnits(units, new SubProgressMonitor(monitor, 1));
        monitor.worked(1);
        monitor.subTask("Uploading files.");
        exporter.exportUnits(project, units, new SubProgressMonitor(monitor, 1));
        monitor.done();
    }

    private void depersonalizeUnits(final List<CompilationUnit> units, final IProgressMonitor monitor) {
        monitor.beginTask("depersonalizing compilationunits", units.size());
        for (final CompilationUnit unit : units) {
            ensureJobIsNotCanceled(monitor);
            monitor.subTask("Depersonalizing " + unit.getName().getIdentifier());
            depersonalize(unit);
            monitor.worked(1);
        }
        monitor.done();
    }

    private void ensureJobIsNotCanceled(final IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    private List<CompilationUnit> deserializeCompilationUnits(final List<IFile> files, final IProgressMonitor monitor) {
        monitor.beginTask("loading compilationunits", files.size());
        final List<CompilationUnit> units = new ArrayList<CompilationUnit>();
        for (final IFile file : files) {
            ensureJobIsNotCanceled(monitor);
            try {
                monitor.subTask("loading file " + file.toString());
                final CompilationUnit unit = getUnit(file);
                if (exportPermitted(unit)) {
                    units.add(unit);
                }
                monitor.worked(1);
            } catch (final Exception e) {
                Activator
                        .getDefault()
                        .getLog()
                        .log(new Status(Status.ERROR, Activator.PLUGIN_ID, "Can't export compilationunit "
                                + file.toString(), e));
            }
        }
        monitor.done();
        return units;
    }

    private boolean exportPermitted(final CompilationUnit unit) {
        final String compilationUnitName = unit.primaryType.name.getIdentifier().substring(1).replace("/", ".");
        return packageTester.matches(compilationUnitName);
    }

    private CompilationUnit depersonalize(CompilationUnit unit) {
        for (final ICompilationUnitDepersonalizer depersonalizer : depersonalizers) {
            unit = depersonalizer.depersonalize(unit);
        }
        return unit;
    }

    private CompilationUnit getUnit(final IFile file) {
        final IPath path = file.getLocation();
        final CompilationUnit unit = GsonUtil.deserialize(path.toFile(), CompilationUnit.class);
        Checks.ensureIsNotNull(unit.id, "The CompilationUnit " + unit.getName() + " has no fingerprint.");

        return unit;
    }

}
