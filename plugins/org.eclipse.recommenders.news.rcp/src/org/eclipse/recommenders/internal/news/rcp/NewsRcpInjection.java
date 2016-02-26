/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 * Pawel Nowak - E4 DI
 */
package org.eclipse.recommenders.internal.news.rcp;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.recommenders.news.rcp.IJobFacade;
import org.eclipse.recommenders.news.rcp.INewsProperties;
import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.recommenders.news.rcp.INotificationFacade;
import org.eclipse.ui.PlatformUI;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class NewsRcpInjection {

    public static final EventBus EVENT_BUS = new EventBus();

    public static void initiateContext(Object object) {
        addBindings();
        ContextInjectionFactory.inject(object,
                (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class));
        ContextInjectionFactory.inject(EVENT_BUS,
                (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class));
    }

    public static void addBindings() {
        InjectorFactory.getDefault().addBinding(INewsService.class).implementedBy(NewsService.class);
        InjectorFactory.getDefault().addBinding(IJobFacade.class).implementedBy(JobFacade.class);
        InjectorFactory.getDefault().addBinding(INewsProperties.class).implementedBy(NewsProperties.class);
        InjectorFactory.getDefault().addBinding(INotificationFacade.class).implementedBy(NotificationFacade.class);
    }
}
