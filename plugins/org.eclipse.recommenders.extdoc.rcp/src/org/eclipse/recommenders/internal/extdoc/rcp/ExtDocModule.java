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
package org.eclipse.recommenders.internal.extdoc.rcp;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.extdoc.rcp.ExtDocPlugin;
import org.eclipse.recommenders.extdoc.rcp.preferences.PreferenceConstants;
import org.eclipse.recommenders.internal.extdoc.rcp.view.ExtDocView;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Google injection configuration.
 */
public final class ExtDocModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UiManager.class).in(Scopes.SINGLETON);
        bind(ProviderStore.class).in(Scopes.SINGLETON);
        bind(ExtDocView.class).in(Scopes.SINGLETON);

        final Named preferenceStoreName = Names.named(PreferenceConstants.NAME_EXTDOC_PREFERENCE_STORE);
        bind(IPreferenceStore.class).annotatedWith(preferenceStoreName).toInstance(ExtDocPlugin.preferenceStore());
    }

}
