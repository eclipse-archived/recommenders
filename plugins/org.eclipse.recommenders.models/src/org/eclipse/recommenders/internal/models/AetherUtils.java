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
package org.eclipse.recommenders.internal.models;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.recommenders.utils.Nullable;

public final class AetherUtils {

    private AetherUtils() {
    }

    public static RemoteRepository createRemoteRepository(String repoId, String uriString) {
        try {
            final URI uri = new URI(uriString);
            final URI uriWithoutUserinfo = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(),
                    uri.getQuery(), uri.getFragment());

            RemoteRepository.Builder builder = new RemoteRepository.Builder(repoId, "default",
                    uriWithoutUserinfo.toString());

            String rawUserInfo = uri.getRawUserInfo();
            if (rawUserInfo != null) {
                final String username;
                final String password;
                int indexOfColon = rawUserInfo.indexOf(':');
                if (indexOfColon < 0) {
                    username = uri.getUserInfo();
                    password = null;
                } else {
                    String rawUsername = rawUserInfo.substring(0, indexOfColon);
                    String rawSsp = uri.getRawSchemeSpecificPart();
                    String rawSspWithoutPassword = "//" + rawUsername + rawSsp.substring(2 + rawUserInfo.length());
                    URI uriWithoutPassword = getSspUri(uri.getScheme(), rawSspWithoutPassword, uri.getRawFragment());
                    username = uriWithoutPassword.getUserInfo();

                    if (indexOfColon == rawUserInfo.length() - 1) {
                        password = null;
                    } else {
                        String rawSspWithoutUsername = "//" + rawSsp.substring(2 + rawUsername.length() + 1);
                        URI uriWithoutUsername = getSspUri(uri.getScheme(), rawSspWithoutUsername,
                                uri.getRawFragment());
                        password = uriWithoutUsername.getUserInfo();
                    }
                }

                Authentication authentication = new AuthenticationBuilder().addUsername(username).addPassword(password)
                        .build();
                builder.setAuthentication(authentication);

            }
            return builder.build();
        } catch (URISyntaxException e) {
            return new RemoteRepository.Builder(repoId, "default", uriString).build();
        }
    }

    private static URI getSspUri(@Nullable String scheme, String rawSsp, @Nullable String rawFragment)
            throws URISyntaxException {
        StringBuilder builder = new StringBuilder();
        if (scheme != null) {
            builder.append(scheme).append(':');
        }
        builder.append(rawSsp);
        if (rawFragment != null) {
            builder.append('#').append(rawFragment);
        }
        return new URI(builder.toString());
    }
}
