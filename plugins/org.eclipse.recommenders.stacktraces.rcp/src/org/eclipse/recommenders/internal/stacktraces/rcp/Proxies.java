/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static com.google.common.base.Optional.*;
import static org.apache.commons.lang3.StringUtils.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.utils.Nullable;

import com.google.common.base.Optional;

public class Proxies {

    private static final String DOUBLEBACKSLASH = "\\\\";
    public static final String ENV_USERDOMAIN = "USERDOMAIN";
    public static final String PROP_HTTP_AUTH_NTLM_DOMAIN = "http.auth.ntlm.domain";

    /**
     * Returns the domain of the current machine- if any.
     *
     * @param userName
     *            the username which (on windows it may contain the domain name as prefix "domain\\username")
     */
    public static Optional<String> getUserDomain(@Nullable String userName) {

        // check the app's system properties
        String domain = System.getProperty(PROP_HTTP_AUTH_NTLM_DOMAIN);
        if (domain != null) {
            return of(domain);
        }

        // check the OS environment
        domain = System.getenv(ENV_USERDOMAIN);
        if (domain != null) {
            return of(domain);
        }

        // test the user's name whether it may contain an information about the domain name
        if (StringUtils.contains(userName, DOUBLEBACKSLASH)) {
            return of(substringBefore(userName, DOUBLEBACKSLASH));
        }

        // no domain name found
        return absent();
    }

    /**
     * Returns the host name of this workstation (localhost)
     */
    public static Optional<String> getWorkstation() {
        try {
            return of(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            return absent();
        }
    }

    /**
     * Returns the user name without a (potential) domain prefix
     *
     * @param userName
     *            a String that may look like "domain\\userName"
     */
    public static Optional<String> getUserName(String userName) {
        if (userName == null) {
            return absent();
        }
        return contains(userName, DOUBLEBACKSLASH) ? of(substringAfterLast(userName, DOUBLEBACKSLASH)) : of(userName);
    }
}
