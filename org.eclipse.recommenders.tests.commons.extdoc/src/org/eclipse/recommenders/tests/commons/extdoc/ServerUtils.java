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
package org.eclipse.recommenders.tests.commons.extdoc;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.rcp.extdoc.preferences.PreferenceConstants;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.UsernameProvider;
import org.mockito.Mockito;

public final class ServerUtils {

    private static ICouchDbServer server;
    private static GenericServer genericServer;
    private static UsernameProvider usernameListener;

    private ServerUtils() {
    }

    public static ICouchDbServer getServer() {
        if (server == null) {
            server = new TestCouchDbServer();
        }
        return server;
    }

    public static UsernameProvider getUsernameListener() {
        if (usernameListener == null) {
            final IPreferenceStore store = Mockito.mock(IPreferenceStore.class);
            Mockito.when(store.getString(PreferenceConstants.USERNAME)).thenReturn("TestUser");
            usernameListener = new UsernameProvider(store);
        }
        return usernameListener;
    }

    public static GenericServer getGenericServer() {
        if (genericServer == null) {
            final UsernameProvider usernameListener = getUsernameListener();
            genericServer = new GenericServer(getServer(), usernameListener, ExtDocUtils.getResolver());
        }
        return genericServer;
    }

}
