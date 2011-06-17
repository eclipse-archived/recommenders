/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.extdoc;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.internal.rcp.extdoc.preferences.PreferenceConstants;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@SuppressWarnings("restriction")
class ClientConfigurationPreferenceListener implements IPropertyChangeListener {

    private final ClientConfiguration config;

    @Inject
    public ClientConfigurationPreferenceListener(
            final @Named(PreferenceConstants.NAME_EXTDOC_WEBSERVICE_CONFIGURATION) ClientConfiguration config,
            @Named(PreferenceConstants.NAME_EXTDOC_PREFERENCE_STORE) final IPreferenceStore store) {
        this.config = config;

        final String host = store.getString(PreferenceConstants.WEBSERVICE_HOST);
        store.addPropertyChangeListener(this);

        config.setBaseUrl(host);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getProperty().equals(PreferenceConstants.WEBSERVICE_HOST)) {
            final Object newValue = event.getNewValue();
            config.setBaseUrl(newValue.toString());
        }
    }

}
