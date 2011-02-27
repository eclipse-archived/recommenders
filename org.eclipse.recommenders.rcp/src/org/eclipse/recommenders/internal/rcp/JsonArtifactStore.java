/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.commons.utils.IOUtils;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.IArtifactStoreChangedListener;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JsonArtifactStore implements IArtifactStore {
    private final Map<Tuple<ICompilationUnit, Class<?>>, Object> index = Maps.newHashMap();

    private final List<IArtifactStoreChangedListener> listener;

    @Inject
    public JsonArtifactStore(final List<IArtifactStoreChangedListener> listener) {
        this.listener = listener;
    }

    @Override
    public boolean hasArtifact(final @Nullable ICompilationUnit cu, final Class<?> clazz) {
        if (cu == null) {
            return false;
        }
        return isArtifactInIndex(cu, clazz) ? true : existsCompilationUnitArtifactFileOnDisk(cu, clazz);
    }

    private <T> boolean isArtifactInIndex(final ICompilationUnit cu, final Class<T> clazz) {
        final Tuple<ICompilationUnit, Class<?>> key = createIndexKey(cu, clazz);
        return index.containsKey(key);
    }

    private boolean existsCompilationUnitArtifactFileOnDisk(final ICompilationUnit cu, final Class<?> clazz) {
        return getCompilationUnitArtifactFile(cu, clazz).exists();
    }

    @Override
    public <T> T loadArtifact(final ICompilationUnit cu, final Class<T> clazz) {
        ensureIsNotNull(cu);
        ensureIsNotNull(clazz);
        ensureIsNotNull(isArtifactInIndex(cu, clazz) || existsCompilationUnitArtifactFileOnDisk(cu, clazz));
        if (isArtifactInIndex(cu, clazz)) {
            return getArtifactInIndex(cu, clazz);
        }
        final IFile file = getCompilationUnitArtifactFile(cu, clazz);
        final T artifact = loadFileContents(clazz, file);
        putArtifactInIndex(cu, clazz, artifact);
        return artifact;
    }

    @SuppressWarnings("unchecked")
    private <T> T getArtifactInIndex(final ICompilationUnit cu, final Class<T> clazz) {
        final Tuple<ICompilationUnit, Class<?>> key = createIndexKey(cu, clazz);
        return (T) index.get(key);
    }

    private Object putArtifactInIndex(final ICompilationUnit cu, final Class<?> clazz, final Object artifact) {
        final Tuple<ICompilationUnit, Class<?>> key = createIndexKey(cu, clazz);
        return index.put(key, artifact);
    }

    private <T> Tuple<ICompilationUnit, Class<?>> createIndexKey(final ICompilationUnit cu, final Class<T> clazz) {
        return Tuple.create(cu, clazz);
    }

    private IFile getCompilationUnitArtifactFile(final ICompilationUnit cu, final Class<?> artifactType) {
        final IFolder folder = getCompilationUnitArtifactsFolder(cu);
        final IFile file = folder.getFile(artifactType.getSimpleName() + ".json");
        return file;
    }

    private IFolder getCompilationUnitArtifactsFolder(final ICompilationUnit unit) {
        final IResource cuFile = unit.getResource();
        final IProject project = cuFile.getProject();
        final IPath cuPath = cuFile.getProjectRelativePath();
        final IPath dataPath = project.getFolder(".recommenders/data").getProjectRelativePath();
        final IPath path = dataPath.append(cuPath);
        final IFolder folder = project.getFolder(path);
        return folder;
    }

    private <T> T loadFileContents(final Class<T> clazz, final IFile file) {
        InputStream in = null;
        try {
            in = file.getContents(true);
            return GsonUtil.deserialize(in, clazz);
        } catch (final CoreException e) {
            RecommendersPlugin.log(e);
            throw throwUnreachable(
                    "the code recommenders builder is mixed up. Perform a clean build. If this exceptions occurs several times, please report a bug.",
                    e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public <T> void storeArtifacts(final ICompilationUnit cu, final List<T> newArtifacts) {
        ensureIsNotNull(cu, "null compilation unit not allowed");
        ensureIsNotNull(newArtifacts, "null artifacts  list not allowed");
        final IFolder folder = getCompilationUnitArtifactsFolder(cu);
        createResource(folder, new NullProgressMonitor());
        for (final Object artifact : newArtifacts) {
            writeArtifactToDisk(folder, artifact);
            updateIndex(cu, artifact);
        }
        fireArtifactsChanged(cu);
    }

    private <T> void updateIndex(final ICompilationUnit cu, final T artifact) {
        final Tuple<ICompilationUnit, Class<?>> key = createIndexKey(cu, artifact.getClass());
        index.put(key, artifact);
    }

    @Override
    public <T> void storeArtifact(final ICompilationUnit cu, final T artifact) {
        final IFolder folder = getCompilationUnitArtifactsFolder(cu);
        createResource(folder, new NullProgressMonitor());
        writeArtifactToDisk(folder, artifact);
        updateIndex(cu, artifact);
        fireArtifactsChanged(cu);
    }

    private void fireArtifactsChanged(final ICompilationUnit cu) {
        new WorkspaceJob(format("Notifying artifact change listeners for '%s' ...", cu.getElementName())) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                monitor.beginTask(taskTitle(), listener.size());
                for (final IArtifactStoreChangedListener l : listener) {
                    monitor.subTask(subtaskTitle(l));
                    l.unitChanged(cu, new SubProgressMonitor(monitor, 1));
                    monitor.worked(1);
                }
                monitor.done();
                return Status.OK_STATUS;
            }

            private String taskTitle() {
                return "Analyzing";
            }

            private String subtaskTitle(final IArtifactStoreChangedListener l) {
                return l.getClass().getSimpleName();
            }
        }.schedule();
    }

    private void writeArtifactToDisk(final IFolder folder, final Object artifact) {
        final String simpleName = artifact.getClass().getSimpleName();
        final IFile file = folder.getFile(simpleName + ".json");
        final String serialize = GsonUtil.serialize(artifact);
        final ByteArrayInputStream source = new ByteArrayInputStream(serialize.getBytes());
        try {
            if (file.exists()) {
                file.setContents(source, true, false, null);
            } else {
                file.create(source, true, null);
            }
        } catch (final Exception x) {
            throwUnhandledException(x);
        }
    }

    @Override
    public void cleanStore(final IProject project) {
        final IFolder folder = project.getFolder(".recommenders/data/");
        if (folder.exists()) {
            try {
                folder.delete(true, null);
            } catch (final Exception x) {
                throwUnhandledException(x);
            }
        }
    }

    @Override
    public void removeArtifacts(final ICompilationUnit cu) {
        try {
            getCompilationUnitArtifactsFolder(cu).delete(true, null);
        } catch (final Exception x) {
            throwUnhandledException(x);
        }
    }

    private void createResource(final IResource resource, final IProgressMonitor monitor) {
        if (resource == null || resource.exists()) {
            return;
        }
        if (!resource.getParent().exists()) {
            createResource(resource.getParent(), monitor);
        }
        try {
            switch (resource.getType()) {
            case IResource.FILE:
                ((IFile) resource).create(new ByteArrayInputStream(new byte[0]), true, monitor);
                break;
            case IResource.FOLDER:
                ((IFolder) resource).create(IResource.DERIVED, true, monitor);
                break;
            case IResource.PROJECT:
                ((IProject) resource).create(monitor);
                ((IProject) resource).open(monitor);
                break;
            }
        } catch (final Exception x) {
            throwUnhandledException(x);
        }
    }
}
