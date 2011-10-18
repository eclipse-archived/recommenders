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

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.udc.ManifestMatchResult;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.commons.analysis.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionModule.UdcServer;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.client.UniformInterfaceException;

public class CallsModelResolverService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ClasspathDependencyStore dependencyStore;
    private final ModelArchiveStore modelStore;
    private final WebServiceClient client;

    @Inject
    public CallsModelResolverService(final ClasspathDependencyStore dependencyStore,
            final ModelArchiveStore modelStore, @UdcServer final ClientConfiguration config) {
        this.dependencyStore = dependencyStore;
        this.modelStore = modelStore;
        client = new WebServiceClient(config);
        client.enableGzipCompression(true);
    }

    public Option<ClasspathDependencyInformation> tryExtractClasspathDependencyInfo(final File file) {
        try {
            return extractClasspathDependencyInfo(file);
        } catch (final IOException e) {
            logger.warn("Unable to extract ClasspathDependencyInformation from package root '%s'", file, e);
            return Option.none();
        }
    }

    private Option<ClasspathDependencyInformation> extractClasspathDependencyInfo(final File file) throws IOException {
        if (isJarFile(file)) {
            final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(file);
            final ClasspathDependencyInformation dependencyInformation = new ClasspathDependencyInformation();
            dependencyInformation.symbolicName = extractor.extractName();
            dependencyInformation.version = extractor.extractVersion();
            dependencyInformation.jarFileFingerprint = extractor.createFingerprint();
            dependencyInformation.jarFileModificationDate = new Date(file.lastModified());
            return Option.wrap(dependencyInformation);
        } else {
            return Option.none();
        }
    }

    private boolean isJarFile(final File file) {
        return file.getName().endsWith(".jar");
    }

    public boolean downloadAndRegisterModel(final File dependencyFile,
            final ClasspathDependencyInformation dependencyInfo) {
        final Option<Manifest> manifest = findManifest(dependencyInfo);
        if (manifest.hasValue()) {
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

    private Option<Manifest> findManifest(final ClasspathDependencyInformation dependencyInfo) {
        final ManifestMatchResult matchResult = client.doPostRequest("manifest", dependencyInfo,
                ManifestMatchResult.class);
        return Option.wrap(matchResult.bestMatch);
    }

    private File downloadModel(final Manifest manifest) throws IOException {
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

    public boolean hasModel(final File file) {
        if (dependencyStore.containsManifest(file)) {
            final Manifest manifest = dependencyStore.getManifest(file);
            final IModelArchive modelArchive = modelStore.getModelArchive(manifest);
            return modelArchive != IModelArchive.NULL;
        }
        return false;
    }

}
