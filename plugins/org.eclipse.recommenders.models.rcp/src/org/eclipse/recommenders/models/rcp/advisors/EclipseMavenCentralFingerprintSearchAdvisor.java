/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models.rcp.advisors;

import static org.eclipse.recommenders.coordinates.maven.MavenCentralFingerprintSearchAdvisor.SEARCH_MAVEN_ORG;

import java.net.URI;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.coordinates.maven.MavenCentralFingerprintSearchAdvisor;
import org.eclipse.recommenders.utils.Urls;

import com.google.common.base.Optional;

public class EclipseMavenCentralFingerprintSearchAdvisor implements IProjectCoordinateAdvisor {

    private static final URI SEARCH_MAVEN_ORG_URI = Urls.toUri(SEARCH_MAVEN_ORG);
    private static final int DEFAULT_PROXY_PORT = 80;

    private final MavenCentralFingerprintSearchAdvisor delegate;
    private final IProxyService proxy;

    private ProxyConfig proxyConfig;

    @Inject
    public EclipseMavenCentralFingerprintSearchAdvisor(IProxyService proxy) {
        this.proxy = proxy;
        ProxyConfig newProxyConfig = updateProxyConfig();
        delegate = new MavenCentralFingerprintSearchAdvisor(newProxyConfig.host, newProxyConfig.port,
                newProxyConfig.user, newProxyConfig.password);
    }

    @Override
    public Optional<ProjectCoordinate> suggest(DependencyInfo dependencyInfo) {
        ProxyConfig updatedProxyConfig = updateProxyConfig();
        if (updatedProxyConfig != null) {
            delegate.setProxy(updatedProxyConfig.host, updatedProxyConfig.port, updatedProxyConfig.user,
                    updatedProxyConfig.password);
        }
        return delegate.suggest(dependencyInfo);
    }

    private ProxyConfig updateProxyConfig() {
        final String currentProxyHost;
        final int currentProxyPort;
        final String currentProxyUser;
        final String currentProxyPassword;

        synchronized (proxy) {
            if (!proxy.isProxiesEnabled()) {
                currentProxyHost = null;
                currentProxyPort = -1;
                currentProxyUser = currentProxyPassword = null;
            } else {
                IProxyData[] entries = proxy.select(SEARCH_MAVEN_ORG_URI);
                if (entries.length == 0) {
                    currentProxyHost = null;
                    currentProxyPort = -1;
                    currentProxyUser = currentProxyPassword = null;
                } else {
                    IProxyData proxyData = entries[0];
                    currentProxyHost = proxyData.getHost();
                    currentProxyPort = proxyData.getPort() != -1 ? proxyData.getPort() : DEFAULT_PROXY_PORT;
                    currentProxyUser = proxyData.getUserId();
                    currentProxyPassword = proxyData.getPassword();
                }
            }
        }

        if (proxyConfig == null
                || !proxyConfig.isEqualTo(currentProxyHost, currentProxyPort, currentProxyUser, currentProxyPassword)) {
            proxyConfig = new ProxyConfig(currentProxyHost, currentProxyPort, currentProxyUser, currentProxyPassword);
            return proxyConfig;
        } else {
            return null;
        }
    }

    /**
     * Encapsulating the four-element proxy configuration in a value object ensures that any concurrent updates to it
     * are atomic.
     */
    private static class ProxyConfig {

        private final String host;
        private final int port;
        private final String user;
        private final String password;

        public ProxyConfig(String host, int port, String user, String password) {
            this.host = host;
            this.port = port;
            this.user = user;
            this.password = password;
        }

        public boolean isEqualTo(String proxyHost, int proxyPort, String proxyUser, String proxyPassword) {
            return StringUtils.equals(host, proxyHost) && port == proxyPort && StringUtils.equals(user, proxyUser)
                    && StringUtils.equals(password, proxyPassword);
        }
    }
}
