/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public final class Uris {

    private Uris() {
    }

    /**
     * Masks the password (if present) in a URI's user information.
     *
     * @param uri
     *            the URI whose user information's password should be masked.
     * @param mask
     *            the character each character in the password will be replaced with. The mask character must be one
     *            valid inside the <a href="https://tools.ietf.org/html/rfc3986#section-3.2.1">user information URI
     *            component</a>.
     * @return the URI with the password masked
     */
    public static String toStringWithMaskedPassword(URI uri, char mask) {
        if (uri.isOpaque()) {
            return uri.toString();
        }

        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            int indexOfColon = userInfo.indexOf(':');
            if (indexOfColon > 0) {
                userInfo = userInfo.substring(0, indexOfColon + 1)
                        + StringUtils.repeat(mask, userInfo.length() - indexOfColon - 1);
            }
        }
        return toStringWithUserInfo(uri, userInfo);
    }

    public static boolean isPasswordProtected(URI uri) {
        if (uri.isOpaque()) {
            return false;
        }
        String userInfo = uri.getUserInfo();
        if (userInfo == null) {
            return false;
        }
        int indexOfColon = userInfo.indexOf(':');
        return indexOfColon > 0;
    }

    public static boolean hasCredentials(URI uri) {
        return uri.getUserInfo() != null;
    }

    public static String toStringWithoutUserinfo(URI uri) {
        if (uri.isOpaque()) {
            return uri.toString();
        }

        return toStringWithUserInfo(uri, null);
    }

    private static String toStringWithUserInfo(URI uri, @Nullable String userInfo) {
        try {
            URI uriWithServerAuthority = uri.parseServerAuthority();
            return new URI(uriWithServerAuthority.getScheme(), userInfo, uriWithServerAuthority.getHost(),
                    uriWithServerAuthority.getPort(), uriWithServerAuthority.getPath(),
                    uriWithServerAuthority.getQuery(), uriWithServerAuthority.getFragment()).toString();
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Optional<URI> parseURI(String uriString) {
        try {
            return Optional.of(new URI(uriString).parseServerAuthority());
        } catch (URISyntaxException e) {
            return Optional.absent();
        }
    }

    public static URI toUri(String uriString) {
        try {
            return new URI(uriString).parseServerAuthority();
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
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

    public static String mangle(URI uri) {
        String string = toStringWithoutUserinfo(uri);
        return string.replaceAll("\\W", "_");
    }
}
