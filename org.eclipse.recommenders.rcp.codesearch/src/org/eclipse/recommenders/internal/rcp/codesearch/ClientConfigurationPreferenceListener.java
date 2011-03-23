package org.eclipse.recommenders.internal.rcp.codesearch;

import static org.eclipse.recommenders.internal.rcp.codesearch.CodesearchModule.NAME_CODESEARCH_PREFERENCE_STORE;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.commons.codesearch.client.ClientConfiguration;
import org.eclipse.recommenders.internal.rcp.codesearch.preferences.PreferenceConstants;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ClientConfigurationPreferenceListener implements IPropertyChangeListener {

    private final ClientConfiguration config;
    private IPreferenceStore store;

    @Inject
    public ClientConfigurationPreferenceListener(final ClientConfiguration config,
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
