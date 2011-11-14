/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.server.proxy;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.server.GuiceInjectableProvider;
import org.eclipse.recommenders.server.ServerConfiguration;
import org.osgi.service.http.HttpService;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

public final class GuiceModule extends AbstractModule {

    private final static String COUCHDB_BASEURL = "org.eclipse.recommenders.server.extdoc.proxy.baseurl";

    private final HttpService httpService;

    public GuiceModule(final HttpService associatedWithHttpService) {
        httpService = associatedWithHttpService;
    }

    @Override
    protected void configure() {
        bind(HttpService.class).toInstance(httpService);

        // needed for JIT binding to select the correct injector:
        bind(GuiceInjectableProvider.class);

        bind(ClientConfiguration.class).annotatedWith(ExtDocScope.class).toInstance(
                ClientConfiguration.create(getCouchDbBaseurl()));
    }

    private String getCouchDbBaseurl() {
        final String url = System.getProperty(COUCHDB_BASEURL, ServerConfiguration.getCouchBaseurl() + "extdoc/");
        return url;
    }

    @Provides
    CouchDBAccessService createDefaultDataAccess(@ExtDocScope final ClientConfiguration config) {
        return new CouchDBAccessService(config);
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface ExtDocScope {
    }

}