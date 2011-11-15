/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.webclient;

import com.google.inject.Singleton;

@Singleton
public class ClientConfiguration {

    public static ClientConfiguration create(final String baseurl) {
        final ClientConfiguration res = new ClientConfiguration();
        res.setBaseUrl(baseurl);
        return res;
    }

    public ClientConfiguration() {
        // Thread.dumpStack();
    }

    private String baseUrl;

    public synchronized String getBaseUrl() {
        return baseUrl;
    }

    public synchronized void setBaseUrl(String newBaseUrl) {
        if (!newBaseUrl.endsWith("/")) {
            newBaseUrl += "/";
        }
        this.baseUrl = newBaseUrl;
    }
}
