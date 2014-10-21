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
package org.eclipse.recommenders.net;

import static com.google.common.base.Optional.*;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.eclipse.core.internal.net.ProxyManager;
import org.eclipse.core.net.proxy.IProxyData;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public final class Proxies {

    private Proxies() {
        throw new IllegalStateException("Not meant to be instantiated");
    }

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
    public static Optional<String> getUserDomain(String userName) {

        // check the app's system properties
        String domain = System.getProperty(PROP_HTTP_AUTH_NTLM_DOMAIN);
        if (domain != null) {
            return of(domain);
        }

        // check the OS environment
        domain = System.getenv(ENV_USERDOMAIN);
        if (domain != null) {
            return of(domain);
        }

        // test the user's name whether it may contain an information about the domain name
        if (StringUtils.contains(userName, DOUBLEBACKSLASH)) {
            return of(substringBefore(userName, DOUBLEBACKSLASH));
        }

        // no domain name found
        return absent();
    }

    /**
     * Returns the host name of this workstation (localhost)
     */
    public static Optional<String> getWorkstation() {
        try {
            return of(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            return absent();
        }
    }

    /**
     * Returns the user name without a (potential) domain prefix
     *
     * @param userName
     *            a String that may look like "domain\\userName"
     */
    public static Optional<String> getUserName(String userName) {
        if (userName == null) {
            return absent();
        }
        return contains(userName, DOUBLEBACKSLASH) ? of(substringAfterLast(userName, DOUBLEBACKSLASH)) : of(userName);
    }

    public static Executor proxy(Executor executor, URI target) {
        IProxyData[] proxies = ProxyManager.getProxyManager().select(target);
        if (isEmpty(proxies)) {
            executor.clearAuth();
        } else {
            IProxyData proxy = proxies[0];
            HttpHost host = new HttpHost(proxy.getHost(), proxy.getPort());
            if (proxy.getUserId() != null) {
                String userId = getUserName(proxy.getUserId()).orNull();
                String pass = proxy.getPassword();
                String workstation = getWorkstation().orNull();
                String domain = getUserDomain(proxy.getUserId()).orNull();
                executor.auth(host, userId, pass, workstation, domain);
            }
        }
        return executor;
    }
}
