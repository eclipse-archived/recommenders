/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.rcp.extdoc.preferences.PreferenceConstants;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.recommenders.server.extdoc.CodeExamplesServer;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.WikiServer;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public final class ExtDocServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ClientConfiguration.class).annotatedWith(
                Names.named(PreferenceConstants.NAME_EXTDOC_WEBSERVICE_CONFIGURATION)).toInstance(
                new ClientConfiguration());

        bind(GenericServer.class).in(Scopes.SINGLETON);
        bind(CodeExamplesServer.class).in(Scopes.SINGLETON);
        bind(SubclassingServer.class).in(Scopes.SINGLETON);
        bind(WikiServer.class).in(Scopes.SINGLETON);

        bind(ClientConfigurationPreferenceListener.class).asEagerSingleton();
        requestStaticInjection(Server.class);
    }

}
