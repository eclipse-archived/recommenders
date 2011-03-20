/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codesearch;

import org.eclipse.recommenders.commons.codesearch.client.ClientConfiguration;
import org.eclipse.recommenders.internal.rcp.codesearch.views.CodesearchController;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class CodesearchModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CodesearchController.class).in(Scopes.SINGLETON);
        bind(ClientConfiguration.class).toInstance(ClientConfiguration.create("http://localhost:29757/codesearch/"));
    }
}
