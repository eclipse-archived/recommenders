/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.codesearch.rcp;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.internal.codesearch.rcp.views.CodesearchController;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class CodesearchModule extends AbstractModule {
    public static final String NAME_CODESEARCH_PREFERENCE_STORE = "codesearch.preferences.store";
    public static final String NAME_CODESEARCH_WEBSERVICE_CONFIGURATION = "codesearch.webservice.configuration";

    @Override
    protected void configure() {

        bind(CodesearchController.class).in(Scopes.SINGLETON);
        bind(ClientConfiguration.class).annotatedWith(Names.named(NAME_CODESEARCH_WEBSERVICE_CONFIGURATION))
                .toInstance(new ClientConfiguration());
        bind(ClientConfigurationPreferenceListener.class).asEagerSingleton();
        bind(IPreferenceStore.class).annotatedWith(Names.named(NAME_CODESEARCH_PREFERENCE_STORE)).toInstance(
                CodesearchPlugin.getDefault().getPreferenceStore());
    }

    @Provides
    CodeSearchClient provide(@Named(NAME_CODESEARCH_WEBSERVICE_CONFIGURATION) final ClientConfiguration config) {
        return new CodeSearchClient(new WebServiceClient(config));
    }
}
