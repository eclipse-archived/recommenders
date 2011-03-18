/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.codesearch.jaxrs;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.recommenders.internal.server.codesearch.IDataAccessService;
import org.eclipse.recommenders.internal.server.codesearch.ISourceUriMapper;
import org.eclipse.recommenders.internal.server.codesearch.LocalSourceUriMapper;
import org.eclipse.recommenders.internal.server.codesearch.couchdb.CouchDbDataAccessService;
import org.eclipse.recommenders.internal.server.codesearch.lucene.LuceneSearchService;
import org.eclipse.recommenders.server.commons.AuthenticationFilter;
import org.eclipse.recommenders.server.commons.GuiceInjectableProvider;
import org.eclipse.recommenders.server.commons.IAuthenticationService;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

public class CodesearchApplication extends DefaultResourceConfig {

    @Inject
    private Injector injector;
    private GuiceInjectableProvider guiceProvider;

    public CodesearchApplication() {
        final HashMap<String, Object> initParams = new HashMap<String, Object>();
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, RolesAllowedResourceFilterFactory.class);
        initParams.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, AuthenticationFilter.class);

        setPropertiesAndFeatures(initParams);
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> result = new HashSet<Class<?>>();
        result.add(CodeSearchResource.class);
        result.add(SourceCodeResource.class);
        result.add(AdminResource.class);
        return result;
    }

    @Override
    public Set<Object> getSingletons() {
        final HashSet<Object> result = new HashSet<Object>();
        result.add(getGuiceProvider());
        return result;
    }

    private GuiceInjectableProvider getGuiceProvider() {
        if (guiceProvider == null) {
            guiceProvider = new GuiceInjectableProvider(createChildInjector());
        }
        return guiceProvider;
    }

    private Injector createChildInjector() {
        return injector.createChildInjector(getModule());
    }

    private Module getModule() {
        return new Module() {

            @Override
            public void configure(final Binder binder) {
                binder.bind(IAuthenticationService.class).to(MockAuthenticationService.class).in(Scopes.SINGLETON);
                binder.bind(IDataAccessService.class).to(CouchDbDataAccessService.class).in(Scopes.SINGLETON);
                binder.bind(File.class).annotatedWith(Names.named("codesearch.basedir")).toInstance(getIndexFolder());
                binder.bind(LuceneSearchService.class).in(Scopes.SINGLETON);
                // binder.bind(ResourceIdentifierService.class).toInstance(new
                // ResourceIdentifierService());

                final MapBinder<String, ISourceUriMapper> sourceUriMapperBinder = MapBinder.newMapBinder(binder,
                        String.class, ISourceUriMapper.class);
                sourceUriMapperBinder.addBinding("local").to(LocalSourceUriMapper.class);
                // mapbinder.addBinding("sourcerer").to(SourcererUriMapper.class);
            }
        };
    }

    protected File getIndexFolder() {
        return new File("codesearch");
    }
}
