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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DefaultDownloadServiceTest {

    private static Server server;
    private static URI serverUri;

    @ClassRule
    public static TemporaryFolder serverRoot = new TemporaryFolder();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0);
        server.addConnector(connector);

        server.setStopAtShutdown(true);

        HandlerList handlerList = new HandlerList();

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(new PathResource(serverRoot.getRoot().toPath()));
        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setHandler(resourceHandler);
        contextHandler.addAliasCheck(new ContextHandler.ApproveAliases());

        handlerList.addHandler(contextHandler);
        handlerList.addHandler(new DefaultHandler());
        server.setHandler(handlerList);

        server.start();

        String host = connector.getHost() == null ? "localhost" : connector.getHost();
        int port = connector.getLocalPort();
        serverUri = new URI(String.format("http://%s:%d/", host, port));
    }

    @Test
    public void testDownloadPresentResource() throws Exception {
        serveResource("present.txt", "content");
        URI uri = serverUri.resolve("present.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        InputStream stream = sut.download(uri, null);

        String content = IOUtils.toString(stream, StandardCharsets.UTF_8.toString());
        assertThat(content, is(equalTo("content")));
    }

    @Test(expected = IOException.class)
    public void testDownloadEmptyResource() throws Exception {
        serveResource("empty.txt", "");
        URI uri = serverUri.resolve("empty.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        sut.download(uri, null);
    }

    @Test(expected = IOException.class)
    public void testDownloadMissingResource() throws Exception {
        URI uri = serverUri.resolve("missing.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        sut.download(uri, null);
    }

    @Test
    public void testReadPresentResource() throws Exception {
        serveResource("present.txt", "content");
        URI uri = serverUri.resolve("present.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        sut.download(uri, null);

        InputStream stream = sut.read(uri);

        String content = IOUtils.toString(stream, StandardCharsets.UTF_8.toString());
        assertThat(content, is(equalTo("content")));
    }

    @Test
    public void testReadPreviouslyPresentResource() throws Exception {
        Path presentTxt = serveResource("present.txt", "content");
        URI uri = serverUri.resolve("present.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        sut.download(uri, null);

        Files.delete(presentTxt);

        try {
            sut.download(uri, null);
            fail("IOException expected");
        } catch (IOException e) {
            // Ignore
        }

        InputStream stream = sut.read(uri);

        String content = IOUtils.toString(stream, StandardCharsets.UTF_8.toString());
        assertThat(content, is(equalTo("content")));
    }

    @Test(expected = IOException.class)
    public void testReadEmptyResource() throws Exception {
        serveResource("empty.txt", "");
        URI uri = serverUri.resolve("empty.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        try {
            sut.download(uri, null);
            fail("IOException expected");
        } catch (IOException e) {
            // Ignore
        }

        sut.read(uri);
    }

    @Test(expected = IOException.class)
    public void testReadMissingResource() throws Exception {
        URI uri = serverUri.resolve("missing.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        try {
            sut.download(uri, null);
            fail("IOException expected");
        } catch (IOException e) {
            // Ignore
        }

        sut.read(uri);
    }

    @Test
    public void testReadNeverDownloadedResource() throws Exception {
        URI uri = serverUri.resolve("never.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        InputStream stream = sut.read(uri);

        assertThat(stream, is(nullValue()));
    }

    @Test
    public void testGetLastAttemptDatePresentResource() throws Exception {
        serveResource("present.txt", "content");
        URI uri = serverUri.resolve("present.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        Date beforeDownload = now(-2, SECONDS); // Adjust by 2s to account for coarse-grained file system timestamps
        sut.download(uri, null);
        Date afterDownload = now(+2, SECONDS); // Adjust by 2s to account for coarse-grained file system timestamps

        Date lastAttemptDate = sut.getLastAttemptDate(uri);

        assertThat(lastAttemptDate, is(greaterThanOrEqualTo(beforeDownload)));
        assertThat(lastAttemptDate, is(lessThanOrEqualTo(afterDownload)));
    }

    @Test
    public void testGetLastAttemptDatePreviouslyPresentResource() throws Exception {
        Path presentTxt = serveResource("present.txt", "content");
        URI uri = serverUri.resolve("present.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        sut.download(uri, null);

        Date previousAttemptDate = sut.getLastAttemptDate(uri);

        Files.delete(presentTxt);

        Thread.sleep(SECONDS.toMillis(2)); // Account for coarse-grained file system timestamps

        try {
            sut.download(uri, null);
            fail("IOException expected");
        } catch (IOException e) {
            // Ignore
        }

        Date lastAttemptDate = sut.getLastAttemptDate(uri);

        assertThat(lastAttemptDate, is(greaterThan(previousAttemptDate)));
    }

    @Test
    public void testGetLastAttemptDateEmptyResource() throws Exception {
        serveResource("empty.txt", "");
        URI uri = serverUri.resolve("empty.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        Date beforeDownload = now(-2, SECONDS); // Adjust by 2s to account for coarse-grained file system timestamps
        try {
            sut.download(uri, null);
            fail("IOException expected");
        } catch (IOException e) {
            // Ignore
        }
        Date afterDownload = now(+2, SECONDS); // Adjust by 2s to account for coarse-grained file system timestamps

        Date lastAttemptDate = sut.getLastAttemptDate(uri);

        assertThat(lastAttemptDate, is(greaterThanOrEqualTo(beforeDownload)));
        assertThat(lastAttemptDate, is(lessThanOrEqualTo(afterDownload)));
    }

    @Test
    public void testGetLastAttemptDateMissingResource() throws Exception {
        URI uri = serverUri.resolve("missing.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        Date beforeDownload = now(-2, SECONDS); // Adjust by 2s to account for coarse-grained file system timestamps
        try {
            sut.download(uri, null);
            fail("IOException expected");
        } catch (IOException e) {
            // Ignore
        }
        Date afterDownload = now(+2, SECONDS); // Adjust by 2s to account for coarse-grained file system timestamps

        Date lastAttemptDate = sut.getLastAttemptDate(uri);

        assertThat(lastAttemptDate, is(greaterThanOrEqualTo(beforeDownload)));
        assertThat(lastAttemptDate, is(lessThanOrEqualTo(afterDownload)));
    }

    @Test
    public void testGetLastAttemptDateNeverDownloadedResource() throws Exception {
        URI uri = serverUri.resolve("never.txt");

        DefaultDownloadService sut = new DefaultDownloadService(temp.getRoot().toPath());

        Date lastAttemptDate = sut.getLastAttemptDate(uri);

        assertThat(lastAttemptDate, is(nullValue()));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        server.stop();
    }

    private Path serveResource(String path, String content) throws IOException {
        Path file = serverRoot.getRoot().toPath().resolve(path);
        Files.newBufferedWriter(file, StandardCharsets.UTF_8).append(content).close();
        return file;
    }

    private Date now(long delta, TimeUnit timeUnit) {
        long now = System.currentTimeMillis();
        return new Date(now + timeUnit.toMillis(delta));
    }
}
