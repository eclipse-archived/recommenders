/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.store2.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveDownloadFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ModelArchiveDownloadRequested;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ModelArchiveDownloadService {
    private final ExecutorService pool = Executors.newFixedThreadPool(5);

    private final WebServiceClient client;
    private final EventBus bus;

    public ModelArchiveDownloadService(final ClientConfiguration webserviceConfig, final EventBus bus) {
        this.bus = bus;
        client = new WebServiceClient(webserviceConfig);
        client.enableGzipCompression(true);
    }

    @Subscribe
    public void onEvent(final ModelArchiveDownloadRequested e) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    File archive = downloadModel(e.manifest);
                    fireModelArchiveDownloaded(archive, e);
                } catch (IOException e1) {
                    RecommendersPlugin.logError(e1, "Exception occurred during model download for %s", e);
                }
            }
        });
    }

    private void fireModelArchiveDownloaded(final File archive, final ModelArchiveDownloadRequested e) {
        ModelArchiveDownloadFinished event = new ModelArchiveDownloadFinished();
        event.archive = archive;
        bus.post(event);
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
}
