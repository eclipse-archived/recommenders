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
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.udc.ManifestMatchResult;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.commons.analysis.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.UdcServer;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.inject.assistedinject.Assisted;
import com.sun.jersey.api.client.UniformInterfaceException;

@SuppressWarnings("restriction")
public class SearchManifestJob extends WorkspaceJob {

    private final File file;
    private final WebServiceClient client;
    private final ClasspathDependencyStore dependencyStore;
    private final ModelArchiveStore modelStore;
    private ClasspathDependencyInformation dependencyInfo;
    private Manifest manifest;
    private IProgressMonitor monitor;

    @Inject
    public SearchManifestJob(@Assisted final File file, final ClasspathDependencyStore dependencyStore,
            final ModelArchiveStore modelStore, @UdcServer final ClientConfiguration config) {
        super(file.getName());
        this.file = file;
        this.dependencyStore = dependencyStore;
        this.modelStore = modelStore;
        client = new WebServiceClient(config);
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
        monitor.beginTask("Begin model lookup for " + file.getPath(), 100);
        findClasspathDependencyInformation();
        monitor.worked(10);
        if (!findManifest()) {
            return;
        }
        monitor.worked(15);
        if (!storeContainsModel()) {
            monitor.subTask("Downloading model...");
            downloadAndRegisterArchive(manifest);
        }
        monitor.worked(75);
        dependencyStore.putManifest(file, manifest);
        monitor.done();
    }

    private void findClasspathDependencyInformation() {
        if (dependencyStore.containsClasspathDependencyInfo(file)) {
            dependencyInfo = dependencyStore.getClasspathDependencyInfo(file);
        } else {
            try {
                monitor.subTask("Extracting classpath dependecy information...");
                dependencyInfo = extractClasspathDependencyInformation();
                if (dependencyInfo != null) {
                    dependencyStore.putClasspathDependencyInfo(file, dependencyInfo);
                }
            } catch (final IOException e) {
                throw Throws.throwUnhandledException(e,
                        "Unable to extract ClasspathDependencyInformation from package root '%s'", file);
            }
        }
    }

    private boolean findManifest() {

        if (dependencyInfo == null) {
            return false;
        }
        monitor.subTask("Looking up manifest using " + client.getBaseUrl() + ".");

        try {
            final ManifestMatchResult matchResult = client.doPostRequest("manifest", dependencyInfo,
                    ManifestMatchResult.class);
            manifest = matchResult.bestMatch;
            return manifest != null;
        } catch (final RuntimeException e) {
            RecommendersPlugin.logError(e, "Error while requesting manifest for classpath dependency '%s'",
                    dependencyInfo);
            return false;
        }
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
            dependencyStore.putManifest(file, manifest);
        } catch (final UniformInterfaceException e) {
            RecommendersPlugin.logError(e, "Error while downloading model for manifest '%s'", manifest.getIdentifier());
        } catch (final IOException e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    private ClasspathDependencyInformation extractClasspathDependencyInformation() throws IOException {
        if (isJarFile()) {
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

    private boolean isJarFile() {
        return file.getName().endsWith(".jar");
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
