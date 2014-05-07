/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.subwords.rcp;

import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IWorkbench;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class SubwordsRcpModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public SubwordsRcpPreferences provide(IWorkbench wb) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        return ContextInjectionFactory.make(SubwordsRcpPreferences.class, context);
    }
}
