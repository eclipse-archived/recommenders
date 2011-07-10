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

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

@SuppressWarnings("restriction")
public final class ServerUtils {

    private static boolean isInit;

    private ServerUtils() {
    }

    public static void initServer() {
        if (!isInit) {
            final ClientConfiguration config = new ClientConfiguration();
            config.setBaseUrl("http://localhost:5984/extdoc");
            Server.setConfig(config, new JavaElementResolver());
            isInit = true;
        }
    }

}
