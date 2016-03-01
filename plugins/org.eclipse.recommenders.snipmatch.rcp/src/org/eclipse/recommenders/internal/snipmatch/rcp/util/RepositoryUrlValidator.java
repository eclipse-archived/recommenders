/**
 * Copyright (c) 2016 Yasett Acurana.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yasett Acurana - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.util;

import java.net.URISyntaxException;

import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportProtocol;
import org.eclipse.jgit.transport.URIish;

public class RepositoryUrlValidator {

    public static boolean isValidUri(String repoUri) {
        try {
            URIish uri = new URIish(repoUri);
            for (TransportProtocol protocol : Transport.getTransportProtocols()) {
                if (protocol.canHandle(uri)) {
                    return true;
                }
            }
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
