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

import java.net.URI;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.recommenders.server.IAuthenticationService;

public class MockAuthenticationService implements IAuthenticationService {
    public String userName = SystemUtils.USER_NAME;

    public Set<String> userRoles = new LinkedHashSet<String>();

    public MockAuthenticationService() {
        this.userRoles.add(IAuthenticationService.ROLE_USER);
        this.userRoles.add(IAuthenticationService.ROLE_ADMIN);
    }

    @Override
    public SecurityContext createSecurityContext(final URI requestUri, final HttpHeaders headers) {
        return new SecurityContext() {
            @Override
            public boolean isUserInRole(final String role) {
                return userRoles.contains(role);
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public Principal getUserPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        final Cookie authorCookie = headers.getCookies().get("author");
                        if (authorCookie != null) {
                            return authorCookie.getValue();
                        }
                        return userName;
                    }
                };
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        };
    }

    @Override
    public ResponseBuilder authenticateResponse(final ResponseBuilder builder, final String identifier) {
        return builder;
    }
}
