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

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.InvalidRequestException;
import org.eclipse.recommenders.commons.client.ServerCommunicationException;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.lfm.LibraryIdentifier;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.commons.analysis.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.internal.commons.analysis.archive.ArchiveMetaData;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.LfmServer;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("restriction")
public class RemoteLibraryIdentifierResolverJob extends WorkspaceJob {

    private final IPackageFragmentRoot packageRoot;
    private final FragmentIndex fragmentIndex;
    private final WebServiceClient client;

    @Inject
    public RemoteLibraryIdentifierResolverJob(@Assisted final IPackageFragmentRoot packageRoot,
            @Assisted final FragmentIndex fragmentIndex, @LfmServer final ClientConfiguration config) {
        super("Resolving name and version of project dependencies");
        this.packageRoot = packageRoot;
        this.fragmentIndex = fragmentIndex;
        client = new WebServiceClient(config);
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        resolve();
        return Status.OK_STATUS;
    }

    private void resolve() {
        if (packageRoot instanceof JarPackageFragmentRoot) {
            try {
                final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(packageRoot.getPath().toFile());
                final ArchiveMetaData metaData = extractor.getArchiveMetaData();
                final LibraryIdentifier libraryIdentifier = client.doPostRequest("libraryIdentifier", metaData,
                        LibraryIdentifier.class);
                if (libraryIdentifier != null) {
                    fragmentIndex.put(packageRoot, libraryIdentifier);
                }
            } catch (final ServerCommunicationException e) {
                RecommendersPlugin.logWarning(e, "Server unreachable");
            } catch (final InvalidRequestException e) {
                RecommendersPlugin.logError(e, "Exception while contacting server to resolve LibraryIdentifier");
            } catch (final IOException e) {
                throw Throws.throwUnhandledException(e);
            }
        }
    }
}
