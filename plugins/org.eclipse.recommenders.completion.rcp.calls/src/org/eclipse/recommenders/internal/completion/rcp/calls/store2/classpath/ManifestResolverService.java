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
package org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.udc.ManifestMatchResult;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionRequested;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ManifestResolverService {

    private final ExecutorService pool = Executors.newFixedThreadPool(5);

    private final EventBus bus;
    private final WebServiceClient client;

    public ManifestResolverService(final ClientConfiguration config, final EventBus bus) {
        this.bus = bus;
        client = new WebServiceClient(config);
        client.enableGzipCompression(true);
    }

    protected void fireNewManifestMappingCreated(final Manifest manifest, final ManifestResolutionRequested request) {
        ManifestResolutionFinished e = new ManifestResolutionFinished();
        e.dependency = request.dependency;
        e.manifestResolverInfo = new ManifestResolverInfo(manifest, request.manuallyTriggered);
        bus.post(e);
    }

    @Subscribe
    public void onEvent(final ManifestResolutionRequested e) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final ManifestMatchResult matchResult = client.doPostRequest("manifest", e.dependency,
                            ManifestMatchResult.class);
                    if (matchResult.bestMatch != null) {
                        fireNewManifestMappingCreated(matchResult.bestMatch, e);
                    }
                } catch (Exception x) {
                    RecommendersPlugin.logError(x, "Failed to resolve manifest for archive '%s'", e.dependency);
                }
            }
        });
    }
}
