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

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public final class Urls {

    @Deprecated
    public static String mangle(URL url) {
        String urlString = Uris.toStringWithoutUserinfo(toUri(url));

        return doMangle(urlString);
    }

    private static String doMangle(String url) {
        return url.replaceAll("\\W", "_");
    }

    @Deprecated
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

    public static Optional<URL> parseURL(String urlString) {
        try {
            return Optional.of(new URL(urlString));
        } catch (MalformedURLException e) {
            return Optional.absent();
        }
    }

    @Deprecated
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
