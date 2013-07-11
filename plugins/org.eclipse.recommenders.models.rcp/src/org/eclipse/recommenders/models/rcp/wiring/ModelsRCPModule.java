/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models.rcp.wiring;

import javax.inject.Singleton;

import org.eclipse.recommenders.models.dependencies.rcp.EclipseDependencyListener;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;

public class ModelsRCPModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        //
    }

    @Singleton
    @Provides
    protected EclipseDependencyListener provideMappingProvider(EventBus bus) {
        return new EclipseDependencyListener(bus);
    }

}
