/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.news.impl.poll;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.recommenders.internal.news.impl.poll.Proxies;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.annotations.VisibleForTesting;

public class DefaultDownloadService implements IDownloadService {

    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    private static final long SOCKET_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    private final String userAgent;

    private final Executor executor = Executor.newInstance();
    private final Path downloadLocation;

    public DefaultDownloadService() {
        this(getStateLocation().resolve("downloads")); //$NON-NLS-1$
    }

    @VisibleForTesting
    DefaultDownloadService(Path downloadLocation) {
        this.downloadLocation = downloadLocation;
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        String symbolicName = bundle.getSymbolicName();
        String version = bundle.getVersion().toString();
        this.userAgent = symbolicName + '/' + version;
    }

    private static Path getStateLocation() {
        Bundle bundle = FrameworkUtil.getBundle(DefaultDownloadService.class);
        return Platform.getStateLocation(bundle).toFile().toPath();
    }

    @Override
    public InputStream download(URI uri, @Nullable IProgressMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            String fileName = mangleUri(uri);
            Path targetPath = downloadLocation.resolve(fileName);
            doDownload(uri, fileName, targetPath, progress.newChild(1));
            return Files.newInputStream(targetPath);
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    private void doDownload(URI uri, String fileName, Path targetFile, SubMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 4);

        Path tempFile = null;
        try (InputStream resourceStream = openWebResourceAsStream(uri, progress.newChild(1))) {
            Files.createDirectories(downloadLocation);

            tempFile = Files.createTempFile(downloadLocation, null, fileName);
            progress.worked(1);

            Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            progress.worked(1);

            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            progress.worked(1);
        } catch (IOException e) {
            progress.setWorkRemaining(0);

            try {
                Files.setLastModifiedTime(targetFile, FileTime.fromMillis(System.currentTimeMillis()));
            } catch (IOException failedToSetLastModifiedTime) {
                e.addSuppressed(failedToSetLastModifiedTime);

                try {
                    Files.createFile(targetFile);
                } catch (IOException failedToCreateFile) {
                    e.addSuppressed(failedToCreateFile);
                }
            }

            throw e;
        } finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    private String mangleUri(URI uri) {
        try {
            return URLEncoder.encode(uri.toASCIIString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(uri.toString(), e);
        }
    }

    private InputStream openWebResourceAsStream(URI uri, SubMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            Request request = Request.Get(uri).viaProxy(Proxies.getProxyHost(uri)).userAgent(userAgent)
                    .connectTimeout((int) CONNECTION_TIMEOUT).staleConnectionCheck(true)
                    .socketTimeout((int) SOCKET_TIMEOUT);
            Response response = Proxies.proxyAuthentication(executor, uri).execute(request);
            HttpResponse returnResponse = response.returnResponse();
            StatusLine statusLine = returnResponse.getStatusLine();
            if (statusLine == null) {
                throw new IOException();
            }
            if (statusLine.getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                throw new IOException(statusLine.getReasonPhrase());
            }
            HttpEntity entity = returnResponse.getEntity();
            if (entity == null || entity.getContentLength() == 0) {
                throw new IOException("Empty representation"); //$NON-NLS-1$
            }
            return entity.getContent();
        } finally {
            progress.worked(1);
        }
    }

    @Override
    @Nullable
    public InputStream read(URI uri) throws IOException {
        String fileName = mangleUri(uri);
        Path targetPath = downloadLocation.resolve(fileName);
        try {
            if (Files.size(targetPath) == 0L) {
                throw new IOException("Empty representation"); //$NON-NLS-1$
            }
            return Files.newInputStream(targetPath);
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    @Nullable
    public Date getLastAttemptDate(URI uri) throws IOException {
        String fileName = mangleUri(uri);
        Path targetPath = downloadLocation.resolve(fileName);

        try {
            return new Date(Files.getLastModifiedTime(targetPath).toMillis());
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw e;
        }
    }
}
