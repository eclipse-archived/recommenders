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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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
    private ClasspathDependencyInformation dependencyInfo;
    private Manifest manifest;

    @Inject
    public SearchManifestJob(@Assisted final IPackageFragmentRoot packageRoot,
            final ClasspathDependencyStore dependencyStore, final ModelArchiveStore modelStore,
            @LfmServer final ClientConfiguration config) {
        super(getJobName(packageRoot));
        this.packageRoot = packageRoot;
        this.dependencyStore = dependencyStore;
        this.modelStore = modelStore;
        client = new WebServiceClient(config);
        setRule(new PackageRootSchedulingRule());
    }

    private static String getJobName(final IPackageFragmentRoot packageRoot) {
        final String filename = packageRoot.getPath().toFile().getName();
        return "Searching recommender models for " + filename;
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        resolve();
        return Status.OK_STATUS;
    }

    private void resolve() {
        findClasspathDependencyInformation();
        if (!findManifest()) {
            return;
        }

        if (!storeContainsModel()) {
            downloadAndRegisterArchive(manifest);
        }

        dependencyStore.putManifest(packageRoot, manifest);
    }

    private void findClasspathDependencyInformation() {
        if (dependencyStore.containsClasspathDependencyInfo(packageRoot)) {
            dependencyInfo = dependencyStore.getClasspathDependencyInfo(packageRoot);
        } else {
            try {
                dependencyInfo = extractClasspathDependencyInformation();
                if (dependencyInfo != null) {
                    dependencyStore.putClasspathDependencyInfo(packageRoot, dependencyInfo);
                }
            } catch (final IOException e) {
                throw Throws.throwUnhandledException(e,
                        "Unable to extract ClasspathDependencyInformation from package root '%s'", packageRoot);
            }
        }
    }

    private boolean findManifest() {
        if (dependencyInfo == null) {
            return false;
        }

        final ManifestMatchResult matchResult = client.doPostRequest("manifest", dependencyInfo,
                ManifestMatchResult.class);
        manifest = matchResult.bestMatch;
        return manifest != null;
    }

    private boolean storeContainsModel() {
        final IModelArchive archive = modelStore.getModelArchive(manifest);
        if (archive == null || archive == IModelArchive.NULL) {
            return false;
        } else {
            return true;
        }
    }

    private void downloadAndRegisterArchive(final Manifest manifest) {
        try {
            final String url = "model/" + WebServiceClient.encode(manifest.getIdentifier());
            final InputStream is = client.createRequestBuilder(url).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .get(InputStream.class);
            final File temp = File.createTempFile("download.", ".zip");
            final FileOutputStream fos = new FileOutputStream(temp);
            IOUtils.copy(is, fos);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(fos);
            modelStore.register(temp);
        } catch (final ServerCommunicationException e) {
            RecommendersPlugin.logWarning(e, "Server unreachable");
        } catch (final InvalidRequestException e) {
            RecommendersPlugin.logError(e, "Exception while contacting server to search for matching Manifest");
        } catch (final IOException e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    private ClasspathDependencyInformation extractClasspathDependencyInformation() throws IOException {
        if (packageRoot instanceof JarPackageFragmentRoot) {
            final File file = packageRoot.getPath().toFile();
            final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(file);
            final ClasspathDependencyInformation dependencyInformation = new ClasspathDependencyInformation();
            dependencyInformation.symbolicName = extractor.extractName();
            dependencyInformation.version = extractor.extractVersion();
            dependencyInformation.jarFileFingerprint = extractor.createFingerprint();
            dependencyInformation.jarFileModificationDate = new Date(file.lastModified());
            return dependencyInformation;
        } else {
            return null;
        }
    }

    private class PackageRootSchedulingRule implements ISchedulingRule {

        @Override
        public boolean contains(final ISchedulingRule rule) {
            return isConflicting(rule);
        }

        @Override
        public boolean isConflicting(final ISchedulingRule rule) {
            if (rule instanceof PackageRootSchedulingRule) {
                final IPackageFragmentRoot otherPackageRoot = ((PackageRootSchedulingRule) rule).getPackageRoot();
                return (otherPackageRoot.equals(packageRoot));
            }
            return false;
        }

        private IPackageFragmentRoot getPackageRoot() {
            return packageRoot;
        }

    }
}
