/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.server.stacktraces.crawler;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.server.stacktraces.crawler.bugzilla.BugzillaCrawler;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

public class CrawlerModule extends AbstractModule {

    @Override
    protected void configure() {
        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBaseUrl("http://localhost:5984/stacktraces/");
        bind(ClientConfiguration.class).toInstance(configuration);
        bind(StorageService.class).in(Scopes.SINGLETON);

        final Multibinder<CrawlerConfiguration> multibinder = Multibinder.newSetBinder(binder(),
                CrawlerConfiguration.class);

        multibinder.addBinding().toInstance(
                new CrawlerConfiguration(BugzillaCrawler.class, "Eclipse", "https://bugs.eclipse.org/bugs/", 1));
    }

}
