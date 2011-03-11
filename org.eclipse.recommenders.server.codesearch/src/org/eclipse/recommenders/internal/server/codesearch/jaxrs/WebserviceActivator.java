package org.eclipse.recommenders.internal.server.codesearch.jaxrs;

import java.util.logging.Logger;

import org.osgi.service.http.HttpService;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class WebserviceActivator {

    private final Logger logger = Logger.getLogger(getClass().getName());
    @Inject(optional = true)
    private final String servicePath = "/codesearch";
    @Inject
    private HttpService httpService;
    @Inject
    private Injector injector;

    public void start() {
        try {
            final ServletContainer servletContainer = new ServletContainer(
                    injector.getInstance(CodesearchApplication.class));
            httpService.registerServlet(servicePath, servletContainer, null, null);
            logger.info("WebService activated and deployed on HttpService: " + httpService);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        httpService.unregister(servicePath);
        logger.info("WebService deactivated.");
    }

}
