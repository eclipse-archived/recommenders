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
package org.eclipse.recommenders.commons.codesearch.client;

import com.google.inject.Singleton;

@Singleton
public class ClientConfiguration {

    public static ClientConfiguration create(final String baseurl) {
        final ClientConfiguration res = new ClientConfiguration();
        res.baseUrl = baseurl;
        return res;
    }

    public ClientConfiguration() {
        System.out.println("client config: " + hashCode());
    }

    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String newBaseUrl) {
        if (!newBaseUrl.endsWith("/")) {
            newBaseUrl += "/";
        }
        this.baseUrl = newBaseUrl;
    }
}
