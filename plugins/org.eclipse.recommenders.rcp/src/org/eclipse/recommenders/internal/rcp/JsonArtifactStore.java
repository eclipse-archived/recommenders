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
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;
import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.IArtifactStoreChangedListener;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.gson.GsonUtil;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JsonArtifactStore implements IArtifactStore {

    ExecutorService writerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Map<Tuple<IJavaElement, Class<?>>, Object> cache = new MapMaker().concurrencyLevel(1).maximumSize(50)
            .makeMap();

    private final List<IArtifactStoreChangedListener> listener;

    @Inject
    public JsonArtifactStore(final List<IArtifactStoreChangedListener> listener) {
        this.listener = listener;
    }

    @Override
    public boolean hasArtifact(final @Nullable IJavaElement cu, final Class<?> clazz) {
        if (cu == null) {
            return false;
        }
        return isArtifactInIndex(cu, clazz) ? true : existsCompilationUnitArtifactFileOnDisk(cu, clazz);
    }

    private <T> boolean isArtifactInIndex(final IJavaElement cu, final Class<T> clazz) {
        final Tuple<IJavaElement, Class<?>> key = createIndexKey(cu, clazz);
        return cache.containsKey(key);
    }

    private boolean existsCompilationUnitArtifactFileOnDisk(final IJavaElement cu, final Class<?> clazz) {
        return getCompilationUnitArtifactFile(cu, clazz).exists();
    }

    @Override
    public <T> T loadArtifact(final IJavaElement cu, final Class<T> clazz) {
        ensureIsNotNull(cu);
        ensureIsNotNull(clazz);
        ensureIsNotNull(isArtifactInIndex(cu, clazz) || existsCompilationUnitArtifactFileOnDisk(cu, clazz));
        if (isArtifactInIndex(cu, clazz)) {
            return getArtifactInIndex(cu, clazz);
        }
        final File file = getCompilationUnitArtifactFile(cu, clazz);
        final T artifact = loadFileContents(clazz, file);
        putArtifactInIndex(cu, clazz, artifact);
        return artifact;
    }

    @SuppressWarnings("unchecked")
    private <T> T getArtifactInIndex(final IJavaElement cu, final Class<T> clazz) {
        final Tuple<IJavaElement, Class<?>> key = createIndexKey(cu, clazz);
        return (T) cache.get(key);
    }

    private Object putArtifactInIndex(final IJavaElement cu, final Class<?> clazz, final Object artifact) {
        final Tuple<IJavaElement, Class<?>> key = createIndexKey(cu, clazz);
        return cache.put(key, artifact);
    }

    private <T> Tuple<IJavaElement, Class<?>> createIndexKey(final IJavaElement cu, final Class<T> clazz) {
        return Tuple.create(cu, clazz);
    }

    private File getCompilationUnitArtifactFile(final IJavaElement cu, final Class<?> artifactType) {
        final File folder = getCompilationUnitArtifactsFolder(cu);
        final File file = new File(folder, artifactType.getSimpleName() + ".json");
        return file;
    }

    private File getCompilationUnitArtifactsFolder(final IJavaElement unit) {
        final IResource cuFile = unit.getResource();
        final IProject project = cuFile.getProject();
        final IPath cuPath = cuFile.getProjectRelativePath();
        final IPath dataPath = project.getFolder(".recommenders/data").getProjectRelativePath();
        final IPath path = dataPath.append(cuPath);
        final File folder = project.getFolder(path).getRawLocation().toFile();
        return folder;
    }

    private <T> T loadFileContents(final Class<T> clazz, final File location) {
        // InputStream in = null;
        try {
            return GsonUtil.deserialize(location, clazz);
            // in = file.getContents(true);

        } catch (final Exception e) {
            RecommendersPlugin
                    .logError(
                            e,
                            "The code recommenders builder is mixed up. Perform a clean build. If this exceptions occurs several times, please report a bug.");
            throw throwUnreachable(
                    "the code recommenders builder is mixed up. Perform a clean build. If this exceptions occurs several times, please report a bug.",
                    e);
        } finally {
            // IOUtils.closeQuietly(in);
        }
    }

    @Override
    public <T> void storeArtifacts(final IJavaElement cu, final List<T> newArtifacts) {
        ensureIsNotNull(cu, "null compilation unit not allowed");
        ensureIsNotNull(newArtifacts, "null artifacts  list not allowed");
        final File folder = getCompilationUnitArtifactsFolder(cu);
        createResource(folder, new NullProgressMonitor());
        writerPool.execute(new Runnable() {

            @Override
            public void run() {
                for (final Object artifact : newArtifacts) {
                    writeArtifactToDisk(folder, artifact);
                    updateIndex(cu, artifact);
                }
                fireArtifactsChanged(cu);
            }
        });

    }

    private <T> void updateIndex(final IJavaElement cu, final T artifact) {
        final Tuple<IJavaElement, Class<?>> key = createIndexKey(cu, artifact.getClass());
        cache.put(key, artifact);
    }

    @Override
    public <T> void storeArtifact(final IJavaElement cu, final T artifact) {
        final File folder = getCompilationUnitArtifactsFolder(cu);
        createResource(folder, new NullProgressMonitor());
        writeArtifactToDisk(folder, artifact);
        updateIndex(cu, artifact);
        fireArtifactsChanged(cu);
    }

    private void fireArtifactsChanged(final IJavaElement cu) {
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

    private void writeArtifactToDisk(final File folder, final Object artifact) {
        ensureIsTrue(folder.exists());
        final String simpleName = artifact.getClass().getSimpleName();
        final File location = new File(folder, simpleName + ".json");
        GsonUtil.serialize(artifact, location);
        // try {
        // final byte[] bytes = GsonUtil.serialize(artifact).getBytes();
        // final InputStream source = new ByteArrayInputStream(bytes);
        // if (file.exists()) {
        // file.setContents(source, true, false, null);
        // } else {
        // file.create(source, true, null);
        // }
        // } catch (final Exception x) {
        // throwUnhandledException(x);
        // }
    }

    @Override
    public void cleanStore(final IProject project) {

        final IPath rawLocation = project.getLocation();
        final File folder = new File(rawLocation.toFile(), ".recommenders/data/");
        if (folder.exists()) {
            try {
                folder.delete();
            } catch (final Exception x) {
                throwUnhandledException(x);
            }
        }
    }

    @Override
    public void removeArtifacts(final IJavaElement cu) {
        try {
            getCompilationUnitArtifactsFolder(cu).delete();
        } catch (final Exception x) {
            throwUnhandledException(x);
        }
    }

    private void createResource(final File resource, final IProgressMonitor monitor) {
        if (resource == null || resource.exists()) {
            return;
        }
        resource.mkdirs();
    }
}
