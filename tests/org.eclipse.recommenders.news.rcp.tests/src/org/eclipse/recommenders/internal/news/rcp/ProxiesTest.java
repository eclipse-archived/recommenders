/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.Proxies.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.fluent.Executor;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.recommenders.testing.RetainSystemProperties;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("restriction")
public class ProxiesTest {

    private static final String EXAMPLE_DOT_COM = "http://www.example.com/";
    private static final int PORT = 8080;

    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String DOMAIN_PROPERTIES = "mydomain_props";

    static final String ENV_USERDOMAIN = "USERDOMAIN";
    static final String PROP_HTTP_AUTH_NTLM_DOMAIN = "http.auth.ntlm.domain";

    @Rule
    public final RetainSystemProperties retainSystemProperties = new RetainSystemProperties();

    @Test
    @Ignore("to make this test work change string mydomain to your hostname")
    public void testUserDomain() {
        assertThat(Proxies.getUserDomain("mydomain\\\\user").orNull(), is(equalTo("mydomain")));

        System.setProperty(PROP_HTTP_AUTH_NTLM_DOMAIN, DOMAIN_PROPERTIES);

        assertThat(Proxies.getUserDomain(DOMAIN_PROPERTIES).orNull(), is(equalTo(DOMAIN_PROPERTIES)));
    }

    @Test
    public void testWorkstation() {
        assertNotNull(Proxies.getWorkstation().orNull());
    }

    @Test
    public void testUserName() {
        assertEquals("user", Proxies.getUserName("\\\\user").get());
        assertEquals("user2", Proxies.getUserName("domain\\\\user2").get());
        assertEquals("user3", Proxies.getUserName("user3").get());
    }

    @Test
    public void testGetProxyHost() throws URISyntaxException {
        URI uri = new URI(EXAMPLE_DOT_COM);
        String host = uri.getHost();

        IProxyData proxyData = new ProxyData(uri.getScheme(), host, PORT, false, null);

        IProxyService service = Mockito.mock(IProxyService.class);
        when(service.select(uri)).thenReturn(new IProxyData[] { proxyData });

        assertThat(Proxies.getProxyHost(service, uri).get(), is(equalTo(new HttpHost(host, PORT))));
    }

    @Test
    public void testProxyAuthentication() throws URISyntaxException, IOException {
        URI uri = new URI(EXAMPLE_DOT_COM);
        String host = uri.getHost();

        IProxyData proxyData = new ProxyData(uri.getScheme(), host, PORT, false, null);
        proxyData.setUserid(USER);
        proxyData.setPassword(PASSWORD);

        IProxyService service = Mockito.mock(IProxyService.class);
        when(service.select(uri)).thenReturn(new IProxyData[] { proxyData });

        Executor executor = mock(Executor.class);
        when(executor.auth(new AuthScope(new HttpHost(host, PORT), AuthScope.ANY_REALM, AuthScope.ANY_SCHEME),
                new UsernamePasswordCredentials(USER, PASSWORD))).thenReturn(executor);
        when(executor.auth(new AuthScope(new HttpHost(host, PORT), AuthScope.ANY_REALM, "ntlm"),
                new NTCredentials(USER, PASSWORD, getWorkstation().orNull(), getUserDomain(USER).orNull())))
                        .thenReturn(executor);

        assertThat(proxyAuthentication(service, executor, uri), is(equalTo(executor)));

        verify(executor).auth(new AuthScope(new HttpHost(host, PORT), AuthScope.ANY_REALM, AuthScope.ANY_SCHEME),
                new UsernamePasswordCredentials(USER, PASSWORD));
        verify(executor).auth(new AuthScope(new HttpHost(host, PORT), AuthScope.ANY_REALM, "ntlm"),
                new NTCredentials(USER, PASSWORD, getWorkstation().orNull(), getUserDomain(USER).orNull()));
    }

    @Test
    public void testProxyAuthenticationNoUserId() throws URISyntaxException, IOException {
        URI uri = new URI(EXAMPLE_DOT_COM);
        IProxyData proxyData = new ProxyData(uri.getScheme(), uri.getHost(), PORT, false, null);
        IProxyService service = Mockito.mock(IProxyService.class);
        when(service.select(uri)).thenReturn(new IProxyData[] { proxyData });

        Executor executor = mock(Executor.class);

        assertThat(proxyAuthentication(service, executor, uri), is(equalTo(executor)));
    }
}
