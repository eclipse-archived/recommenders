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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.CALLS_STORE_LOCATION;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.CallsModelIndex;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchive;

import com.google.common.collect.Lists;

/**
 * Performs a pre-fetching of all available types from store...
 */
public class ModelStoreInitializerJob extends WorkspaceJob {

    @Inject
    @Named(CALLS_STORE_LOCATION)
    private File modelArchiveLocation;
    @Inject
    private CallsModelIndex modelIndex;

    @Inject
    public ModelStoreInitializerJob() {
        super("Initalizing Calls Recommender...");
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Indexing available recommender models...", 1);
        InjectionService.getInstance().injectMembers(this);

        try {
            initializeModelIndex();
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    private void initializeModelIndex() {
        final List<ModelArchive> loadArchives = loadArchives();
        for (final ModelArchive modelArchive : loadArchives) {
            modelIndex.register(modelArchive);
        }
    }

    private List<ModelArchive> loadArchives() {
        final List<ModelArchive> archives = Lists.newLinkedList();
        final Collection<File> files = FileUtils.listFiles(modelArchiveLocation,
                FileFilterUtils.suffixFileFilter(".zip"), TrueFileFilter.INSTANCE);
        for (final File file : files) {
            archives.add(new ModelArchive(file));
        }
        return archives;
    }
}
