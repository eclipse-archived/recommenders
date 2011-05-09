package org.eclipse.recommenders.server.stacktraces.crawler;

import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.server.stacktraces.crawler.bugzilla.BugzillaCrawler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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

        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBaseUrl("http://localhost:5984/stacktraces/");
        final StorageService storageService = new StorageService(configuration);

        final Thread bugzillaCrawlerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final BugzillaCrawler crawler = new BugzillaCrawler(storageService);

                    crawler.loadAllBugs(new GregorianCalendar(2011, 0, 1).getTime(),
                            new GregorianCalendar(2011, 4, 9).getTime());
                } catch (final JAXBException e) {
                    e.printStackTrace();
                }

                storageService.shutdown();
            }
        });
        bugzillaCrawlerThread.start();
    }

    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
