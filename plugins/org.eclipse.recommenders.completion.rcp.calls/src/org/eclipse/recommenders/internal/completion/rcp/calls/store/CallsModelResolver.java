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
package org.eclipse.recommenders.internal.completion.rcp.calls.store;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.udc.ManifestMatchResult;
import org.eclipse.recommenders.internal.analysis.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.internal.completion.rcp.calls.CallsCompletionModule.UdcServer;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.Throws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.jersey.api.client.UniformInterfaceException;

public class CallsModelResolver {

    public static enum OverridePolicy {
        NONE, ALL, MANIFEST
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ClasspathDependencyStore dependencyStore;
    private final ModelArchiveStore modelStore;
    private final WebServiceClient client;

    @Inject
    public CallsModelResolver(final ClasspathDependencyStore dependencyStore, final ModelArchiveStore modelStore,
            @UdcServer final ClientConfiguration config) {
        this.dependencyStore = dependencyStore;
        this.modelStore = modelStore;
        client = new WebServiceClient(config);
        client.enableGzipCompression(true);
    }

    public void resolve(final File file, final CallsModelResolver.OverridePolicy overridePolicy) {
        final Optional<ClasspathDependencyInformation> dependencyInfo = findClasspathDependencyInformation(file,
                overridePolicy);
        if (dependencyInfo.isPresent()) {
            findModel(file, overridePolicy, dependencyInfo.get());
        }
    }

    private Optional<ClasspathDependencyInformation> findClasspathDependencyInformation(final File file,
            final OverridePolicy overridePolicy) {
        if (overridePolicy != CallsModelResolver.OverridePolicy.ALL
                && dependencyStore.containsClasspathDependencyInfo(file)) {
            return fromNullable(dependencyStore.getClasspathDependencyInfo(file));
        } else {
            final Optional<ClasspathDependencyInformation> dependencyInfo = tryExtractClasspathDependencyInfo(file);
            if (dependencyInfo.isPresent()) {
                dependencyStore.putClasspathDependencyInfo(file, dependencyInfo.get());
            }
            return dependencyInfo;
        }
    }

    private void findModel(final File file, final OverridePolicy overridePolicy,
            final ClasspathDependencyInformation dependencyInfo) {
        if (overridePolicy == CallsModelResolver.OverridePolicy.NONE && doesStoreContainModel(file)) {
            return;
        }
        downloadAndRegisterModel(file, dependencyInfo);
    }

    public Optional<ClasspathDependencyInformation> tryExtractClasspathDependencyInfo(final File file) {
        try {
            return extractClasspathDependencyInfo(file);
        } catch (final IOException e) {
            logger.warn("Unable to extract ClasspathDependencyInformation from package root '%s'", file, e);
            return Optional.absent();
        }
    }

    private Optional<ClasspathDependencyInformation> extractClasspathDependencyInfo(final File file) throws IOException {
        if (isJarFile(file)) {
            final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(file);
            final ClasspathDependencyInformation dependencyInformation = new ClasspathDependencyInformation();
            dependencyInformation.symbolicName = extractor.extractName();
            dependencyInformation.version = extractor.extractVersion();
            dependencyInformation.jarFileFingerprint = extractor.createFingerprint();
            dependencyInformation.jarFileModificationDate = new Date(file.lastModified());
            return of(dependencyInformation);
        } else {
            return absent();
        }
    }

    private boolean isJarFile(final File file) {
        return file.getName().endsWith(".jar");
    }

    public boolean downloadAndRegisterModel(final File dependencyFile,
            final ClasspathDependencyInformation dependencyInfo) {
        final Optional<Manifest> manifest = findManifest(dependencyInfo);
        if (manifest.isPresent()) {
            try {
                final File modelFile = downloadModel(manifest.get());
                modelStore.register(modelFile);
                dependencyStore.putManifest(dependencyFile, manifest.get());
                return true;
            } catch (final UniformInterfaceException e) {
                RecommendersPlugin.logError(e, "Error while downloading model for manifest '%s'", manifest.get()
                        .getIdentifier());
            } catch (final IOException e) {
                throw Throws.throwUnhandledException(e);
            }
        }
        return false;
    }

    protected Optional<Manifest> findManifest(final ClasspathDependencyInformation dependencyInfo) {
        final ManifestMatchResult matchResult = client.doPostRequest("manifest", dependencyInfo,
                ManifestMatchResult.class);
        return fromNullable(matchResult.bestMatch);
    }

    protected File downloadModel(final Manifest manifest) throws IOException {
        final String url = "model/" + WebServiceClient.encode(manifest.getIdentifier());
        final InputStream is = client.createRequestBuilder(url).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get(InputStream.class);
        final File temp = File.createTempFile("download.", ".zip");
        final FileOutputStream fos = new FileOutputStream(temp);
        IOUtils.copy(is, fos);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(fos);
        return temp;
    }

    public boolean doesStoreContainModel(final File file) {
        if (dependencyStore.containsManifest(file)) {
            final Manifest manifest = dependencyStore.getManifest(file);
            final IModelArchive modelArchive = modelStore.getModelArchive(manifest);
            return modelArchive != IModelArchive.NULL;
        }
        return false;
    }

}
