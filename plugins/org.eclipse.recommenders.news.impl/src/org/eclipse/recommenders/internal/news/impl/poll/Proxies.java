/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.impl.poll;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.http.auth.AuthScope.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.fluent.Executor;
import org.eclipse.core.internal.net.ProxyManager;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

@SuppressWarnings("restriction")
public final class Proxies {

    private Proxies() {
        // Not meant to be instantiated
    }

    private static final String NTLM_SCHEME = "ntlm"; //$NON-NLS-1$
    private static final String DOUBLEBACKSLASH = "\\\\"; //$NON-NLS-1$
    private static final String ENV_USERDOMAIN = "USERDOMAIN"; //$NON-NLS-1$
    private static final String PROP_HTTP_AUTH_NTLM_DOMAIN = "http.auth.ntlm.domain"; //$NON-NLS-1$

    /**
     * Returns the domain of the current machine- if any.
     *
     * @param userName
     *            the user name which may be null. On windows it may contain the domain name as prefix
     *            "domain\\username".
     */
    @Nullable
    public static String getUserDomain(String userName) {

        // check the app's system properties
        String domain = System.getProperty(PROP_HTTP_AUTH_NTLM_DOMAIN);
        if (domain != null) {
            return domain;
        }

        // check the OS environment
        domain = System.getenv(ENV_USERDOMAIN);
        if (domain != null) {
            return domain;
        }

        // test the user's name whether it may contain an information about the domain name
        if (StringUtils.contains(userName, DOUBLEBACKSLASH)) {
            return substringBefore(userName, DOUBLEBACKSLASH);
        }

        // no domain name found
        return null;
    }

    /**
     * Returns the host name of this workstation (localhost)
     */
    @Nullable
    public static String getWorkstation() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Returns the user name without a (potential) domain prefix
     *
     * @param userName
     *            a String that may look like "domain\\userName"
     */
    @Nullable
    public static String getUserName(@Nullable String userName) {
        if (userName == null) {
            return null;
        }
        return contains(userName, DOUBLEBACKSLASH) ? substringAfterLast(userName, DOUBLEBACKSLASH) : userName;
    }

    @Nullable
    public static HttpHost getProxyHost(URI target) {
        return getProxyHost(ProxyManager.getProxyManager(), target);
    }

    @VisibleForTesting
    @Nullable
    static HttpHost getProxyHost(IProxyService proxyService, URI target) {
        IProxyData proxy = getProxyData(proxyService, target);
        if (proxy == null) {
            return null;
        }
        return new HttpHost(proxy.getHost(), proxy.getPort());
    }

    public static Executor proxyAuthentication(Executor executor, URI target) throws IOException {
        return proxyAuthentication(ProxyManager.getProxyManager(), executor, target);
    }

    @VisibleForTesting
    static Executor proxyAuthentication(IProxyService proxyService, Executor executor, URI target) throws IOException {
        IProxyData proxy = getProxyData(proxyService, target);

        if (proxy == null) {
            return executor;
        }

        String userId = proxy.getUserId();
        if (userId == null) {
            return executor;
        }

        String userName = getUserName(userId);
        String password = proxy.getPassword();
        String workstation = getWorkstation();
        String domain = getUserDomain(userId);

        HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort());
        return executor
                .auth(new AuthScope(proxyHost, ANY_REALM, NTLM_SCHEME),
                        new NTCredentials(userName, password, workstation, domain))
                .auth(new AuthScope(proxyHost, ANY_REALM, ANY_SCHEME),
                        new UsernamePasswordCredentials(userName, password));
    }

    @Nullable
    private static IProxyData getProxyData(IProxyService service, URI target) {
        if (target == null || service == null) {
            return null;
        }

        IProxyData[] proxies = service.select(target);
        if (isEmpty(proxies)) {
            return null;
        }
        return proxies[0];
    }
}
