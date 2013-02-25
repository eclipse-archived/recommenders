/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Gottschaemmer, Olav Lenz - initial Implementation.
 */
package org.eclipse.recommenders.tests.rcp.repo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.recommenders.internal.rcp.repo.ServiceBasedProxySelector;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RemoteRepository;

public class ServiceBasedProxySelectorTest {

    private static final boolean ENABLED = true;
    private static final boolean DISABLED = false;

    private static final IProxyService NO_PROXY_SERVICE = null;

    @Test
    public void testNoProxyService() {
        ProxySelector sut = new ServiceBasedProxySelector(NO_PROXY_SERVICE);
        Proxy proxy = sut.getProxy(newRemoteRepository("http://example.org/"));

        assertThat(proxy, is(nullValue()));
    }

    @Test
    public void testProxiesDisabled() {
        IProxyService service = mockProxyService(DISABLED);

        ProxySelector sut = new ServiceBasedProxySelector(service);
        Proxy proxy = sut.getProxy(newRemoteRepository("http://example.org/"));

        assertThat(proxy, is(nullValue()));
    }

    @Test
    public void testNoProxies() {
        IProxyService service = mockProxyService(ENABLED);

        ProxySelector sut = new ServiceBasedProxySelector(service);
        Proxy proxy = sut.getProxy(newRemoteRepository("http://example.org/"));

        assertThat(proxy, is(nullValue()));
    }

    @Test
    public void testSingleProxy() {
        IProxyData mock = mockProxyData("HTTP", "example.com", 8080);
        IProxyService service = mockProxyService(ENABLED, mock);

        ProxySelector sut = new ServiceBasedProxySelector(service);
        Proxy proxy = sut.getProxy(newRemoteRepository("http://example.org/"));

        assertThat(proxy.getType(), is(equalTo("http")));
        assertThat(proxy.getHost(), is(equalTo("example.com")));
        assertThat(proxy.getPort(), is(equalTo(8080)));
    }

    @Test
    public void testMultipleProxies() {
        IProxyData first = mockProxyData("HTTP", "example.com", 8080);
        IProxyData second = mockProxyData("HTTP", "example.net", 8081);
        IProxyService service = mockProxyService(ENABLED, first, second);

        ProxySelector sut = new ServiceBasedProxySelector(service);
        Proxy proxy = sut.getProxy(newRemoteRepository("http://example.org/"));

        assertThat(proxy.getType(), is(equalTo("http")));
        assertThat(proxy.getHost(), is(equalTo("example.com")));
        assertThat(proxy.getPort(), is(equalTo(8080)));
    }

    @Test
    public void testNonProxyableUrl() {
        IProxyService service = mockThrowingProxyService(ENABLED);

        ProxySelector sut = new ServiceBasedProxySelector(service);
        Proxy proxy = sut.getProxy(newRemoteRepository("file:/tmp"));

        assertThat(proxy, is(nullValue()));
    }

    private IProxyData mockProxyData(String type, String host, int port) {
        IProxyData proxyData = mock(IProxyData.class);
        when(proxyData.getType()).thenReturn(type);
        when(proxyData.getHost()).thenReturn(host);
        when(proxyData.getPort()).thenReturn(port);
        return proxyData;
    }

    private IProxyService mockProxyService(boolean proxyEnabled, IProxyData... proxies) {
        IProxyService proxyService = mock(IProxyService.class);
        when(proxyService.isProxiesEnabled()).thenReturn(proxyEnabled);
        when(proxyService.select(Mockito.any(URI.class))).thenReturn(proxies);
        return proxyService;
    }

    private IProxyService mockThrowingProxyService(boolean proxyEnabled) {
        IProxyService proxyService = mock(IProxyService.class);
        when(proxyService.isProxiesEnabled()).thenReturn(proxyEnabled);
        when(proxyService.select(Mockito.any(URI.class))).thenThrow(new RuntimeException());
        return proxyService;
    }

    private RemoteRepository newRemoteRepository(String url) {
        return new RemoteRepository("test", "default", url);
    }
}
