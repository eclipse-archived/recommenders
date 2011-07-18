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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
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
import org.eclipse.recommenders.commons.lfm.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.lfm.ManifestMatchResult;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.commons.analysis.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.LfmServer;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("restriction")
public class SearchManifestJob extends WorkspaceJob {

    private final IPackageFragmentRoot packageRoot;
    private final WebServiceClient client;
    private final ClasspathDependencyStore dependencyStore;
    private final ModelArchiveStore modelStore;

    @Inject
    public SearchManifestJob(@Assisted final IPackageFragmentRoot packageRoot,
            final ClasspathDependencyStore dependencyStore, final ModelArchiveStore modelStore,
            @LfmServer final ClientConfiguration config) {
        super("Resolving name and version of project dependencies");
        this.packageRoot = packageRoot;
        this.dependencyStore = dependencyStore;
        this.modelStore = modelStore;
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
                final ClasspathDependencyInformation dependencyInformation = extractClasspathDependencyInformation();
                dependencyStore.putClasspathDependencyInfo(packageRoot, dependencyInformation);
                final ManifestMatchResult matchResult = client.doPostRequest("manifest", dependencyInformation,
                        ManifestMatchResult.class);
                final Manifest manifest = matchResult.bestMatch;
                if (manifest != null) {
                    if (!storeContainsModel(manifest)) {
                        downloadAndRegisterArchive(manifest);
                    }
                    dependencyStore.putManifest(packageRoot, manifest);
                }
            } catch (final ServerCommunicationException e) {
                RecommendersPlugin.logWarning(e, "Server unreachable");
            } catch (final InvalidRequestException e) {
                RecommendersPlugin.logError(e, "Exception while contacting server to search for matching Manifest");
            } catch (final IOException e) {
                throw Throws.throwUnhandledException(e);
            }
        }
    }

    private boolean storeContainsModel(final Manifest manifest) {
        final IModelArchive archive = modelStore.getModelArchive(manifest);
        if (archive == null || archive == IModelArchive.NULL) {
            return false;
        } else {
            return true;
        }
    }

    private void downloadAndRegisterArchive(final Manifest manifest) throws IOException, FileNotFoundException {
        final String url = "model/" + WebServiceClient.encode(manifest.getIdentifier());
        final InputStream is = client.createRequestBuilder(url).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get(InputStream.class);
        final File temp = File.createTempFile("download.", ".zip");
        final FileOutputStream fos = new FileOutputStream(temp);
        IOUtils.copy(is, fos);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(fos);
        modelStore.register(temp);
    }

    private ClasspathDependencyInformation extractClasspathDependencyInformation() throws IOException {
        final File file = packageRoot.getPath().toFile();
        final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(file);
        final ClasspathDependencyInformation dependencyInformation = new ClasspathDependencyInformation();
        dependencyInformation.symbolicName = extractor.extractName();
        dependencyInformation.version = extractor.extractVersion();
        dependencyInformation.jarFileFingerprint = extractor.createFingerprint();
        dependencyInformation.jarFileModificationDate = new Date(file.lastModified());
        return dependencyInformation;
    }
}
