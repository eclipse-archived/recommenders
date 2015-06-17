/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.ui.IWorkbench;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class NewsRcpModule extends AbstractModule {

    @Override
    protected void configure() {
        // no-op
    }

    @Provides
    @Singleton
    NewsRcpPreferences providePreferences(IWorkbench wb) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        return ContextInjectionFactory.make(NewsRcpPreferences.class, context);
    }

    @Provides
    @Singleton
    INewsService provideNewsService(NewsRcpPreferences preferences, EventBus eventBus, NewsFeedProperties properties,
            IJobFacade jobFacade) {
        return new NewsService(preferences, eventBus, properties, jobFacade);
    }

    @Provides
    @Singleton
    IJobFacade provideJobFacade() {
        return new JobFacade();
    }

}
