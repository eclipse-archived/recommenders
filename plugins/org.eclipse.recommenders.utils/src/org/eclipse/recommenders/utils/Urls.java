/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public final class Urls {

    public static String mangle(URL url) {
        int len = url.getProtocol().length() + 1;

        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();
        String query = url.getQuery();
        String ref = url.getRef();

        if (host != null && host.length() > 0) {
            len += 2 + host.length();
        }
        if (port >= 0) {
            len += 1 + String.valueOf(port).length();
        }
        if (path != null) {
            len += path.length();
        }
        if (query != null) {
            len += 1 + query.length();
        }
        if (ref != null) {
            len += 1 + ref.length();
        }

        StringBuffer result = new StringBuffer(len);
        result.append(url.getProtocol());
        result.append(":");
        if (host != null && host.length() > 0) {
            result.append("//");
            result.append(host);
        }
        if (port >= 0) {
            result.append(":");
            result.append(String.valueOf(port));
        }
        if (path != null) {
            result.append(path);
        }
        if (query != null) {
            result.append('?');
            result.append(query);
        }
        if (ref != null) {
            result.append("#");
            result.append(ref);
        }

        return doMangle(result.toString());
    }

    private static String doMangle(String url) {

        return url.replaceAll("\\W", "_");
    }

    public static String mangle(String urlString) {
        URL url = parseURL(urlString).orNull();
        if (url == null) {
            return doMangle(urlString);
        } else {
            return mangle(url);
        }
    }

    public static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    public static URI toUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    private static Optional<URL> parseURL(String urlString) {
        try {
            return Optional.of(new URL(urlString));
        } catch (MalformedURLException e) {
            return Optional.absent();
        }
    }

    public static Optional<URI> parseURI(String uriString) {
        try {
            return Optional.of(new URI(uriString));
        } catch (URISyntaxException e) {
            return Optional.absent();
        }
    }

    public static boolean isUriProtocolSupported(URI uri, List<String> protocols) {
        for (String protocol : protocols) {
            if (StringUtils.equalsIgnoreCase(protocol, uri.getScheme())) {
                return true;
            }
        }

        return false;
    }

    public static URL getUrl(File file) {
        URI uri = file.toURI();

        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    private Urls() {
    }
}
