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
import java.lang.reflect.InvocationTargetException;
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
        try (@Nullable InputStream resourceStream = openWebResourceAsStream(uri, progress.newChild(1))) {
            if (resourceStream == null) {
                progress.setWorkRemaining(0);

                try {
                    updateLastModifiedTime(targetFile);
                    return;
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                }
            }

            Files.createDirectories(downloadLocation);

            tempFile = Files.createTempFile(downloadLocation, null, fileName);
            progress.worked(1);

            Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            progress.worked(1);

            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            progress.worked(1);
        } catch (InvocationTargetException e) {
            throw (IOException) e.getTargetException();
        } catch (IOException e) {
            progress.setWorkRemaining(0);

            try {
                updateLastModifiedTime(targetFile);
            } catch (IOException e2) {
                e.addSuppressed(e2);
            }

            throw e;
        } finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    private void updateLastModifiedTime(Path targetFile) throws IOException {
        try {
            Files.setLastModifiedTime(targetFile, FileTime.fromMillis(System.currentTimeMillis()));
        } catch (IOException e) {
            try {
                Files.createFile(targetFile);
            } catch (IOException e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }

    private String mangleUri(URI uri) {
        try {
            return URLEncoder.encode(uri.toASCIIString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(uri.toString(), e);
        }
    }

    /**
     * @return an input stream for the Web resource at the given URI or <code>null</code>, if the Web resource has not
     *         been modified since the {@linkplain #getLastAttemptDate(URI) last download attempt}.
     */
    @Nullable
    private InputStream openWebResourceAsStream(URI uri, SubMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            Request request = Request.Get(uri).viaProxy(Proxies.getProxyHost(uri)).userAgent(userAgent)
                    .connectTimeout((int) CONNECTION_TIMEOUT).staleConnectionCheck(true)
                    .socketTimeout((int) SOCKET_TIMEOUT);
            try {
                // This errs if the last attempt was a failure but the web resource has been modified in the meantime.
                // In that case, we will receive a 304 Not Modified, but won't have an up-to-date representation at
                // head but only the representation (if any) retrieved prior to the failed attempt.
                // This problem is relatively benign, however, and fixing it requires larger changes (keeping separate
                // dates for successful and failed attempts).
                Date lastAttemptDate = getLastAttemptDate(uri);
                if (lastAttemptDate != null) {
                    request.setIfModifiedSince(lastAttemptDate);
                }
            } catch (IOException e) {
                // Ignore
            }

            Response response = Proxies.proxyAuthentication(executor, uri).execute(request);
            HttpResponse returnResponse = response.returnResponse();
            StatusLine statusLine = returnResponse.getStatusLine();
            if (statusLine == null) {
                throw new IOException();
            }
            if (statusLine.getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                throw new IOException(statusLine.getReasonPhrase());
            } else if (statusLine.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
                return null;
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
