/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.preferences;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

public class ClientConfigurationPreferenceListener implements IPropertyChangeListener {

    private final ClientConfiguration config;

    public ClientConfigurationPreferenceListener(final ClientConfiguration config, final IPreferenceStore store) {
        this.config = config;

        final String host = store.getString(PreferenceConstants.WEBSERVICE_HOST);
        store.addPropertyChangeListener(this);

        checkIsHostValid(host);
        config.setBaseUrl(host);
    }

    private void checkIsHostValid(final String host) {
        try {
            new URL(host);
        } catch (final MalformedURLException e) {
            RecommendersPlugin.logError(e, "server url for codesearch server is invalid: '%s'", host);
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
