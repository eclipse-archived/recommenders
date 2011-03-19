/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.codesearch;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsFalse;

public class Constants {

    private static final String PREFIX = "org.eclipse.recommenders.server.codesearch";
    public static String WEB_BASE_URL = defaultString(getProperty(PREFIX + ".baseUrl"),
            "http://localhost:29757/codesearch");
    public static String COUCHDB_USER = getProperty(PREFIX + ".couchdb.username");
    public static String COUCHDB_PASSWORD = getProperty(PREFIX + ".couchdb.password");

    static {
        // make small sanity check:
        ensureIsFalse(WEB_BASE_URL.isEmpty(), "no server url given");
        ensureIsFalse(WEB_BASE_URL.endsWith("/"),
                "we assume that no trailing slash is used internally. Otherwise url concatenations dont work as expected.");
    }
}
