package org.eclipse.recommenders.server.codesearch;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.recommenders.server.codesearch.couchdb.CouchDbDataAccessService;
import org.eclipse.recommenders.server.codesearch.couchdb.IDataAccessService;
import org.eclipse.recommenders.server.codesearch.resources.CodeSearchResource;
import org.eclipse.recommenders.server.codesearch.resources.SourceCodeResource;
import org.eclipse.recommenders.server.commons.AuthenticationFilter;
import org.eclipse.recommenders.server.commons.GuiceInjectableProvider;
import org.eclipse.recommenders.server.commons.IAuthenticationService;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
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
                binder.bind(SearchService.class).in(Scopes.SINGLETON);
                // binder.bind(ResourceIdentifierService.class).toInstance(new
                // ResourceIdentifierService());
            }
        };
    }

    protected File getIndexFolder() {
        return new File("codesearch");
    }
}
