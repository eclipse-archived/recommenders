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
package org.eclipse.recommenders.internal.server.extdoc;

import java.util.HashMap;
import java.util.Map;

public final class Server {

    // TODO: Temporary fix for demo and tests. Will be replaced with CouchDB
    // client.
    private static final Map<String, Map<String, Object>> CACHE = new HashMap<String, Map<String, Object>>();

    private Server() {
    }

    public static Map<String, Object> getDocument(final String docId) {
        return CACHE.get(docId);
    }

    public static void storeOrUpdateDocument(final String docId, final Map<String, Object> attributes) {
        CACHE.put(docId, attributes);
    }

}
