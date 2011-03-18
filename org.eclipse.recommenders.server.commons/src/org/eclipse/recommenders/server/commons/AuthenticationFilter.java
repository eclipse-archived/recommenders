package org.eclipse.recommenders.server.commons;

import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private IAuthenticationService authService;

    @Override
    public ContainerRequest filter(final ContainerRequest request) {
        request.setSecurityContext(authService.createSecurityContext(request.getRequestUri(), request));
        return request;
    }

}
