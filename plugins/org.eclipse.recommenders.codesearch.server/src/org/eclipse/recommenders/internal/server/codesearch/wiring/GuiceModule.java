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
package org.eclipse.recommenders.internal.server.codesearch.wiring;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.recommenders.internal.server.codesearch.CouchDbDataAccessService;
import org.eclipse.recommenders.internal.server.codesearch.IDataAccessService;
import org.eclipse.recommenders.internal.server.codesearch.ISourceUriMapper;
import org.eclipse.recommenders.internal.server.codesearch.LocalSourceUriMapper;
import org.eclipse.recommenders.internal.server.codesearch.lucene.LuceneSearchService;
import org.eclipse.recommenders.server.GuiceInjectableProvider;
import org.eclipse.recommenders.server.ServerConfiguration;
import org.eclipse.recommenders.utils.Throws;
import org.osgi.service.http.HttpService;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

public final class GuiceModule extends AbstractModule {

    private final static String COUCHDB_BASEURL = "org.eclipse.recommenders.server.codesearch.couchdb.baseurl";
    private static final String CONTEXT_PATH = "codesearch/";
    private final HttpService httpService;

    public GuiceModule(final HttpService httpService) {
        this.httpService = ensureIsNotNull(httpService);
    }

    @Override
    protected void configure() {
        bind(HttpService.class).toInstance(httpService);
        bind(IDataAccessService.class).to(CouchDbDataAccessService.class).in(Scopes.SINGLETON);
        bind(LuceneSearchService.class).in(Scopes.SINGLETON);
        bind(GuiceInjectableProvider.class).in(Scopes.SINGLETON);
        bind(File.class).annotatedWith(CodesearchBasedir.class).toInstance(getCodesearchBasedir());
        bind(URL.class).annotatedWith(CodesearchBaseurl.class).toInstance(getCodesearchBaseurl());
        bind(URL.class).annotatedWith(CodesearchCouchDbBaseurl.class).toInstance(getCodesearchCouchDbBaseurl());
        bindSourceUriMapper();
    }

    private void bindSourceUriMapper() {
        final MapBinder<String, ISourceUriMapper> sourceUriMapperBinder = MapBinder.newMapBinder(binder(),
                String.class, ISourceUriMapper.class);
        sourceUriMapperBinder.addBinding("local").to(LocalSourceUriMapper.class);
        // mapbinder.addBinding("sourcerer").to(SourcererUriMapper.class);
    }

    protected File getCodesearchBasedir() {
        final File basedir = new File(ServerConfiguration.getDataBasedir(), CONTEXT_PATH);
        basedir.mkdir();
        return basedir;
    }

    private URL getCodesearchCouchDbBaseurl() {
        final String url = System.getProperty(COUCHDB_BASEURL, ServerConfiguration.getCouchBaseurl() + CONTEXT_PATH);
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw Throws.throwUnhandledException(e, "Failed to couchdb base url: '%s'", url);
        }
    }

    private URL getCodesearchBaseurl() {
        try {
            return new URL(ServerConfiguration.getHttpBaseurl(), CONTEXT_PATH);
        } catch (final MalformedURLException e) {
            throw Throws.throwUnhandledException(e, "Failed to create new url from '%s' + '%s'",
                    ServerConfiguration.getHttpBaseurl(), CONTEXT_PATH);
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER })
    @Inherited
    @BindingAnnotation
    public static @interface CodesearchBasedir {

    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER })
    @Inherited
    @BindingAnnotation
    public static @interface CodesearchBaseurl {

    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER })
    @Inherited
    @BindingAnnotation
    public static @interface CodesearchCouchDbBaseurl {

    }

}