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

import static org.eclipse.recommenders.internal.rcp.codesearch.CodesearchModule.NAME_CODESEARCH_PREFERENCE_STORE;
import static org.eclipse.recommenders.internal.rcp.codesearch.CodesearchModule.NAME_CODESEARCH_WEBSERVICE_CONFIGURATION;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.internal.rcp.codesearch.preferences.PreferenceConstants;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ClientConfigurationPreferenceListener implements IPropertyChangeListener {

    private final ClientConfiguration config;
    private final IPreferenceStore store;

    @Inject
    public ClientConfigurationPreferenceListener(
            final @Named(NAME_CODESEARCH_WEBSERVICE_CONFIGURATION) ClientConfiguration config,
            @Named(NAME_CODESEARCH_PREFERENCE_STORE) final IPreferenceStore store) {
        this.config = config;
        this.store = store;

        final String host = store.getString(PreferenceConstants.WEBSERVICE_HOST);
        store.addPropertyChangeListener(this);

        checkIsHostValid(host);
        config.setBaseUrl(host);
    }

    private void checkIsHostValid(final String host) {
        try {
            new URL(host);
        } catch (final MalformedURLException e) {
            CodesearchPlugin.logError(e, "server url for codesearch server is invalid: '%s'", host);
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getProperty().equals(PreferenceConstants.WEBSERVICE_HOST)) {
            final Object newValue = event.getNewValue();
            config.setBaseUrl(newValue.toString());
        }

    }

}
