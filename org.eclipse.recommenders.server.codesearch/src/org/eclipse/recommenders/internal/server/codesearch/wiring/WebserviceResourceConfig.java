/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.codesearch.wiring;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.recommenders.commons.client.GsonProvider;
import org.eclipse.recommenders.server.codesearch.resources.AdminResource;
import org.eclipse.recommenders.server.codesearch.resources.CodeSearchResource;
import org.eclipse.recommenders.server.codesearch.resources.SourceCodeResource;
import org.eclipse.recommenders.server.commons.GuiceInjectableProvider;

import com.google.inject.Inject;
import com.sun.jersey.api.core.DefaultResourceConfig;

public class WebserviceResourceConfig extends DefaultResourceConfig {

    private final GuiceInjectableProvider guiceProvider;

    @Inject
    public WebserviceResourceConfig(final GuiceInjectableProvider guiceProvider) {
        this.guiceProvider = guiceProvider;
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> result = new HashSet<Class<?>>();
        result.add(CodeSearchResource.class);
        result.add(SourceCodeResource.class);
        result.add(AdminResource.class);
        return result;
    }

    @Override
    public Set<Object> getSingletons() {
        final HashSet<Object> result = new HashSet<Object>();
        result.add(guiceProvider);
        result.add(new GsonProvider());
        return result;
    }
}
