/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.preferences;

import static java.lang.Integer.parseInt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocPlugin;
import org.eclipse.recommenders.utils.Throws;
import org.osgi.service.prefs.BackingStoreException;

public class PreferencesFacade {

    private final IEclipsePreferences preferences;

    private static final String PROVIDER_NAMES = "providerNames";
    private static final String DISABLED_PROVIDERS = "disabledProviders";
    private static final String SASH_WEIGHTS = "sashWeights";

    public PreferencesFacade() {
        preferences = InstanceScope.INSTANCE.getNode(ExtdocPlugin.PLUGIN_ID);
    }

    public void storeOrderedProviders(final List<ExtdocProvider> extdocProviders) {
        final String[] providerNames = getProviderNames(extdocProviders);
        final String toSave = createString(providerNames);
        preferences.put(PROVIDER_NAMES, toSave);
        flush();
    }

    private static String[] getProviderNames(final List<ExtdocProvider> extdocProviders) {
        final String[] names = new String[extdocProviders.size()];
        int i = 0;
        for (final ExtdocProvider p : extdocProviders) {
            names[i] = p.getDescription().getName();
            i++;
        }
        return names;
    }

    private String createString(final String[] names) {
        String out = "";
        for (final String name : names) {
            out += "," + name;
        }
        return out.substring(1);
    }

    public String[] loadOrderedProviderNames() {
        final String orderedNameString = preferences.get(PROVIDER_NAMES, "");
        final String[] orderedNames = orderedNameString.split(",");
        return orderedNames;
    }

    public boolean isProviderEnabled(final ExtdocProvider p) {
        final String arrayString = preferences.get(DISABLED_PROVIDERS, "");
        final String[] deactivatedProviders = arrayString.split(",");
        final String providerName = p.getDescription().getName();
        for (final String deactivatedName : deactivatedProviders) {
            if (deactivatedName.equals(providerName)) {
                return false;
            }
        }
        return true;
    }

    public void storeProviderEnablement(final List<ExtdocProvider> extdocProviders) {
        final String[] disabledProviderNames = getDisabledProviderNames(extdocProviders);
        final String toSave = createString(disabledProviderNames);
        preferences.put(DISABLED_PROVIDERS, toSave);
        flush();
    }

    private String[] getDisabledProviderNames(final List<ExtdocProvider> extdocProviders) {
        final List<String> disabledProviders = new ArrayList<String>();
        for (final ExtdocProvider p : extdocProviders) {
            if (!p.isEnabled()) {
                disabledProviders.add(p.getDescription().getName());
            }
        }
        return disabledProviders.toArray(new String[0]);
    }

    public int[] loadSashWeights() {
        final String weightString = preferences.get(SASH_WEIGHTS, "1,3");
        final String[] weights = weightString.split(",");
        return new int[] { parseInt(weights[0]), parseInt(weights[1]) };
    }

    public void storeSashWeights(final int[] weights) {
        final String toSave = weights[0] + "," + weights[1];
        preferences.put(SASH_WEIGHTS, toSave);
        flush();
    }

    private void flush() {
        try {
            preferences.flush();
        } catch (final BackingStoreException e) {
            Throws.throwUnhandledException(e);
        }
    }
}