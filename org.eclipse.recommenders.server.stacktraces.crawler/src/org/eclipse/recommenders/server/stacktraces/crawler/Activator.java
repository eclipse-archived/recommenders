package org.eclipse.recommenders.server.stacktraces.crawler;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Activator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        Activator.context = bundleContext;

        final Injector injector = Guice.createInjector(new CrawlerModule());
        final CrawlerDaemon daemon = injector.getInstance(CrawlerDaemon.class);

        final Thread bugzillaCrawlerThread = new Thread(daemon);
        bugzillaCrawlerThread.start();
    }

    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
