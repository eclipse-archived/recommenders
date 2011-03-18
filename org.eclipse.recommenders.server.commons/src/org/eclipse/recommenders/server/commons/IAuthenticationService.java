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
