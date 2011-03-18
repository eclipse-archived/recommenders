package org.eclipse.recommenders.internal.server.codesearch;

import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.server.codesearch.jaxrs.WebserviceActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

public class Activator implements BundleActivator {

    private ServiceTracker tracker;
    private HttpService httpService;
    private WebserviceActivator serviceActivator;

    @Override
    public void start(final BundleContext context) throws Exception {
        System.err.println("Bundle started");
        tracker = new ServiceTracker(context, HttpService.class.getName(), null) {
            @Override
            public Object addingService(final ServiceReference serviceRef) {
                System.err.println("HttpService found");
                httpService = (HttpService) super.addingService(serviceRef);
                startService();
                return httpService;
            }

            @Override
            public void removedService(final ServiceReference ref, final Object service) {
                if (httpService == service) {
                    stopService();
                    httpService = null;
                }
                super.removedService(ref, service);
            }
        };
        tracker.open();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        tracker.close();
        stopService();
    }

    private void stopService() {
        if (serviceActivator != null) {
            serviceActivator.stop();
        }
    }

    private void startService() {
        final Injector injector = InjectionService.getInstance().getInjector()
                .createChildInjector(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HttpService.class).toInstance(httpService);
                    }
                });

        serviceActivator = injector.getInstance(WebserviceActivator.class);
        try {
            serviceActivator.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
