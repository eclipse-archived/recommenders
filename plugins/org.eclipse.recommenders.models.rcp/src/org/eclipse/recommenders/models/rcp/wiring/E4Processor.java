/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.models.rcp.wiring;

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.models.rcp.ModelArchiveProvider;
import org.eclipse.recommenders.models.rcp.ProjectCoordinateProvider;

public class E4Processor {

    @PostConstruct
    public void postConstruct(IEclipseContext context) {
        context.set(ProjectCoordinateProvider.class, new ProjectCoordinateProvider());
        ModelArchiveProvider provider = ContextInjectionFactory.make(ModelArchiveProvider.class, context);
        context.set(ModelArchiveProvider.class, provider);
    }
}
