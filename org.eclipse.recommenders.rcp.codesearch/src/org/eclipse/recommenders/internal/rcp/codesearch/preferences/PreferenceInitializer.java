/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.commons.codesearch.client.ClientConfiguration;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;

import com.google.inject.Inject;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Inject
    private ClientConfiguration config;

    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore preferenceStore = CodesearchPlugin.getDefault().getPreferenceStore();
        preferenceStore.setDefault(PreferenceConstants.WEBSERVICE_HOST, config.getBaseUrl());
    }
}
