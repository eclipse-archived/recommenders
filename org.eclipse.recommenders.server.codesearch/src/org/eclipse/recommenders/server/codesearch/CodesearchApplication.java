package org.eclipse.recommenders.server.codesearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.recommenders.server.commons.AuthenticationFilter;
import org.eclipse.recommenders.server.commons.GuiceInjectableProvider;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
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
                // binder.bind(ResourceIdentifierService.class).toInstance(new
                // ResourceIdentifierService());
            }
        };
    }
}
