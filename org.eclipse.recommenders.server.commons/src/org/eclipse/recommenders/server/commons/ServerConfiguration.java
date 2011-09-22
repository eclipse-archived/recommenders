/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.server.commons;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsDirectory;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.recommenders.commons.utils.Throws;

public class ServerConfiguration {

    private static final String SERVER_DATA_BASEDIR = "org.eclipse.recommenders.server.basedir";
    private static final String SERVER_HTTP_BASEURL = "org.eclipse.recommenders.server.http.baseurl";
    private static final String SERVER_COUCHDB_BASEURL = "org.eclipse.recommenders.server.couchdb.baseurl";

    private static final String DEFAULT_BASEDIR_PREFIX = "recommenders";
    public static final int DEFAULT_HTTP_PORT = 29750;
    private static final String DEFAULT_HTTP_HOST = "http://localhost" + ":" + DEFAULT_HTTP_PORT;

    /**
     * Returns an absolute path to the code recommenders data base directory. It
     * looks for the system property {@value #SERVER_DATA_BASEDIR} and, if set,
     * returns a file representing this path - or
     * {@code $working_directory/DEFAULT_BASEDIR_PREFIX} otherwise.
     * 
     * <p>
     * The directory is created on first access.
     */
    public static File getDataBasedir() {
        final String path = System.getProperty(SERVER_DATA_BASEDIR, DEFAULT_BASEDIR_PREFIX);
        final File basedir = new File(path).getAbsoluteFile();
        if (basedir.exists()) {
            ensureIsDirectory(basedir);
        } else {
            ensureIsTrue(
                    basedir.mkdirs(),
                    "Failed to create recommenders data base directory or any of its parents: %s. Are write rights granted? ",
                    basedir);
        }
        return basedir;
    }

    // public static int getHttpPort() {
    // final String port =
    // System.getProperty("org.eclipse.equinox.http.jetty.http.port",
    // DEFAULT_HTTP_PORT);
    // return Integer.parseInt(port);
    // }

    public static URL getHttpBaseurl() {
        final String url = System.getProperty(SERVER_HTTP_BASEURL, DEFAULT_HTTP_HOST);
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw Throws.throwUnhandledException(e, "failed to compute http baseurl.");
        }
    }

    public static URL getCouchBaseurl() {
        final String url = System.getProperty(SERVER_COUCHDB_BASEURL, "http://localhost:5984/");
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw Throws.throwUnhandledException(e, "failed to compute http baseurl.");
        }
    }

}
