/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.codesearch.server.wiring;

import java.net.URL;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.internal.codesearch.server.wiring.GuiceModule.CodesearchBaseurl;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.google.inject.Inject;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class WebserviceActivator {

    private final WebserviceResourceConfig app;
    private final HttpService httpService;
    private final String servicePath;

    @Inject
    public WebserviceActivator(final WebserviceResourceConfig app, final HttpService httpService,
            @CodesearchBaseurl final URL baseurl) {
        this.app = app;
        this.httpService = httpService;

        // JAX-RS is a bit picky about service paths:
        // remove the trailing '/' if exists
        this.servicePath = StringUtils.removeEnd(baseurl.getPath(), "/");
    }

    public void start() throws ServletException, NamespaceException {
        final ServletContainer servletContainer = new ServletContainer(app);
        httpService.registerServlet(servicePath, servletContainer, null, null);
    }

    public void stop() {
        httpService.unregister(servicePath);
    }
}
