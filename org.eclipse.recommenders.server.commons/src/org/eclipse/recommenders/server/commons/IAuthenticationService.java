/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.server.commons;

import java.net.URI;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;

public interface IAuthenticationService {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    SecurityContext createSecurityContext(URI requestUri, HttpHeaders headers);

    ResponseBuilder authenticateResponse(ResponseBuilder builder, String identifier);

}
