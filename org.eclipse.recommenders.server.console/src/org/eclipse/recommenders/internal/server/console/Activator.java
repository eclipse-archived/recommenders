package org.eclipse.recommenders.internal.server.console;

import java.util.Properties;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(final BundleContext context) throws Exception {
        final String serviceName = CommandProvider.class.getName();
        final RecommendersCommandProvider commandProvider = new RecommendersCommandProvider();
        final Properties properties = new Properties();
        context.registerService(serviceName, commandProvider, properties);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
    }

}
