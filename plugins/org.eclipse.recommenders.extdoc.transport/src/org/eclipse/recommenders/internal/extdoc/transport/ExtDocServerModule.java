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
package org.eclipse.recommenders.internal.extdoc.transport;

import org.eclipse.recommenders.extdoc.rcp.feedback.IUserFeedbackServer;
import org.eclipse.recommenders.extdoc.rcp.preferences.PreferenceConstants;
import org.eclipse.recommenders.extdoc.transport.ICouchDbServer;
import org.eclipse.recommenders.extdoc.transport.UsernameProvider;
import org.eclipse.recommenders.webclient.ClientConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Binds dependencies required by server classes.
 */
public final class ExtDocServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ClientConfiguration.class).annotatedWith(
                Names.named(PreferenceConstants.NAME_EXTDOC_WEBSERVICE_CONFIGURATION)).toInstance(
                new ClientConfiguration());
        bind(ICouchDbServer.class).to(CouchDbServer.class);
        bind(IUserFeedbackServer.class).to(FeedbackServer.class);

        bind(ClientConfigurationPreferenceListener.class).asEagerSingleton();
        bind(UsernameProvider.class).asEagerSingleton();
    }

}
