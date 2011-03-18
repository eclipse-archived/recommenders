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

import org.apache.commons.lang3.StringUtils;

public class Constants {

    public static String WEB_BASE_URL = StringUtils.defaultString(
            System.getProperty("org.recommenders.server.codesearch.baseUrl"), "http://localhost:8080/codesearch/");
    public static String COUCHDB_USER = System.getProperty("org.recommenders.server.codesearch.couchdb.username");
    public static String COUCHDB_PASSWORD = System.getProperty("org.recommenders.server.codesearch.couchdb.password");
}
