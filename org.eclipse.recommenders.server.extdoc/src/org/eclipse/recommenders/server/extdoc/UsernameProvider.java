/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.server.extdoc;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.rcp.extdoc.preferences.PreferenceConstants;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class UsernameProvider implements IPropertyChangeListener {

    private String username;

    @Inject
    public UsernameProvider(
            @Named(PreferenceConstants.NAME_EXTDOC_PREFERENCE_STORE) final IPreferenceStore store) {
        username = Preconditions.checkNotNull(store.getString(PreferenceConstants.USERNAME));
        store.addPropertyChangeListener(this);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getProperty().equals(PreferenceConstants.USERNAME)) {
            final Object newValue = event.getNewValue();
            username = Preconditions.checkNotNull(newValue.toString());
        }
    }

}
