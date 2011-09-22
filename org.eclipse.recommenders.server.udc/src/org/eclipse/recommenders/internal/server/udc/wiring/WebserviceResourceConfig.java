/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.udc.wiring;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.recommenders.commons.client.GsonProvider;
import org.eclipse.recommenders.server.commons.GuiceInjectableProvider;
import org.eclipse.recommenders.server.udc.resources.ManifestResource;
import org.eclipse.recommenders.server.udc.resources.ModelResource;
import org.eclipse.recommenders.server.udc.resources.UploadResource;

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
        result.add(ModelResource.class);
        result.add(UploadResource.class);
        result.add(ManifestResource.class);
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
