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

import org.eclipse.recommenders.internal.server.extdoc.AbstractFeedbackServer;
import org.eclipse.recommenders.rcp.extdoc.features.IUserFeedbackServer;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.UsernameProvider;

public final class ServerUtils {

    private static ICouchDbServer server = new TestCouchDbServer();
    private static IUserFeedbackServer genericServer;
    private static UsernameProvider usernameListener;

    private ServerUtils() {
    }

    public static ICouchDbServer getServer() {
        return server;
    }

    public static UsernameProvider getUsernameListener() {
        if (usernameListener == null) {
            usernameListener = new UsernameProvider(null) {
                @Override
                public final String getUsername() {
                    return "TestUser";
                }
            };
        }
        return usernameListener;
    }

    public static IUserFeedbackServer getGenericServer() {
        if (genericServer == null) {
            final UsernameProvider listener = getUsernameListener();
            genericServer = new AbstractFeedbackServer(getServer(), listener) {
            };
        }
        return genericServer;
    }

}
