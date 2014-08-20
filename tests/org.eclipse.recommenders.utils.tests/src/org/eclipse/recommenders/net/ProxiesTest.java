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
package org.eclipse.recommenders.net;

import static org.junit.Assert.*;

import org.junit.Test;

// XXX a single test does not justify a new test plugin yet. This may be changed later
public class ProxiesTest {

    static final String ENV_USERDOMAIN = "USERDOMAIN";
    static final String PROP_HTTP_AUTH_NTLM_DOMAIN = "http.auth.ntlm.domain";

    @Test
    public void testUserDomain() {
        assertEquals("mydomain", Proxies.getUserDomain("mydomain\\\\user").orNull());

        // System.getenv().put(ProxyUtils.ENV_USERDOMAIN, "mydomain_env\\\\user");
        // assertEquals(null, ProxyUtils.getUserDomain("mydomain_env\\\\user").orNull());

        System.getProperties().put(PROP_HTTP_AUTH_NTLM_DOMAIN, "mydomain_props");
        assertEquals("mydomain_props", Proxies.getUserDomain("mydomain_props").orNull());

    }

    @Test
    public void testWorkstation() {
        assertNotNull(Proxies.getWorkstation().orNull());
    }

    @Test
    public void testUserName() {
        assertEquals("user", Proxies.getUserName("\\\\user").get());
        assertEquals("user2", Proxies.getUserName("domain\\\\user2").get());
        assertEquals("user3", Proxies.getUserName("user3").get());
    }
}
