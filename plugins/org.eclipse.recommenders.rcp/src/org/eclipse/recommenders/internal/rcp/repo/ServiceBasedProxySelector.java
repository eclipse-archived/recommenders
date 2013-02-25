/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Patrick Gottschaemmer, Olav Lenz - introduced ProxySelector
 */
package org.eclipse.recommenders.internal.rcp.repo;

import java.net.URI;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RemoteRepository;

public class ServiceBasedProxySelector implements ProxySelector {

    private final IProxyService proxyService;

    public ServiceBasedProxySelector(IProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @Override
    public Proxy getProxy(RemoteRepository remote) {
        if (proxyService == null)
            return null;

        if (!proxyService.isProxiesEnabled())
            return null;

        URI uri = URI.create(remote.getUrl());
        if (uri.getHost() == null)
            return null; // IProxyService requires a host to select a proxy

        IProxyData[] entries = proxyService.select(uri);
        if (entries.length == 0)
            return null;

        IProxyData proxyData = entries[0];
        String type = proxyData.getType().toLowerCase();
        String host = proxyData.getHost();
        int port = proxyData.getPort();
        Authentication auth = new Authentication(proxyData.getUserId(), proxyData.getPassword());
        return new Proxy(type, host, port, auth);
    }
}
