package org.eclipse.recommenders.commons.codesearch.client;

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource.Builder;

public class WebServiceClient {

    private final ClientConfiguration configuration;
    private final Client client;
    private final Map<String, Cookie> cookies;

    @Inject
    public WebServiceClient(final ClientConfiguration configuration) {
        this.configuration = configuration;
        this.client = new Client();
        cookies = new LinkedHashMap<String, Cookie>();
    }

    public String getBaseUrl() {
        return configuration.getBaseUrl();
    }

    public Builder createRequestBuilder(final String path) {
        return addCookies(client.resource(getBaseUrl() + path).accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON));
    }

    private Builder addCookies(final Builder builder) {
        for (final Cookie cookie : cookies.values()) {
            builder.cookie(cookie);
        }
        return builder;
    }

    public <T> T doGetRequest(final String path, final Class<T> resultType) throws NotFoundException,
            ServerUnreachableException, ServerErrorException {
        try {
            return createRequestBuilder(path).get(resultType);
        } catch (final RuntimeException e) {
            return handleGetRequestException(e);
        }
    }

    public <T> T doGetRequest(final String path, final GenericType<T> genericType) throws NotFoundException,
            ServerUnreachableException, ServerErrorException {
        try {
            return createRequestBuilder(path).get(genericType);
        } catch (final RuntimeException e) {
            return handleGetRequestException(e);
        }
    }

    public <T> T doPutRequest(final String path, final Object requestEntity, final Class<T> resultType)
            throws NotFoundException, ConflictException, UnauthorizedAccessException, ServerErrorException,
            ServerUnreachableException {
        try {
            return createRequestBuilder(path).put(resultType, requestEntity);
        } catch (final RuntimeException e) {
            return handlePutAndPostRequestException(e);
        }
    }

    public <T> T doPostRequest(final String path, final Object requestEntity, final Class<T> resultType)
            throws NotFoundException, ConflictException, UnauthorizedAccessException, ServerErrorException,
            ServerUnreachableException {
        try {
            return createRequestBuilder(path).post(resultType, requestEntity);
        } catch (final RuntimeException e) {
            return handlePutAndPostRequestException(e);
        }
    }

    public void doPostRequest(final String path, final Object requestEntity) {
        createRequestBuilder(path).post(requestEntity);
    }

    public <T> T doDeleteRequest(final String path, final Class<T> resultType) throws NotFoundException,
            UnauthorizedAccessException, ServerErrorException, ServerUnreachableException {
        try {
            return createRequestBuilder(path).delete(resultType);
        } catch (final RuntimeException e) {
            return handleDeleteRequestException(e);
        }
    }

    private <T> T handleGetRequestException(final RuntimeException e) throws NotFoundException,
            ServerUnreachableException, ServerErrorException {
        if (e instanceof UniformInterfaceException) {
            switch (((UniformInterfaceException) e).getResponse().getClientResponseStatus()) {
            case NOT_FOUND:
                throw new NotFoundException(e);
            default:
                throw new ServerErrorException(e);
            }
        } else if (e instanceof ClientHandlerException) {
            throw new ServerUnreachableException(e);
        }
        throw e;
    }

    private <T> T handlePutAndPostRequestException(final RuntimeException e) throws NotFoundException,
            ConflictException, UnauthorizedAccessException, ServerErrorException, ServerUnreachableException {
        if (e instanceof UniformInterfaceException) {
            switch (((UniformInterfaceException) e).getResponse().getClientResponseStatus()) {
            case NOT_FOUND:
                throw new NotFoundException(e);
            case CONFLICT:
                throw new ConflictException(e);
            case UNAUTHORIZED:
            case FORBIDDEN:
                throw new UnauthorizedAccessException(e);
            default:
                throw new ServerErrorException(e);
            }
        } else if (e instanceof ClientHandlerException) {
            throw new ServerUnreachableException(e);
        }
        throw e;
    }

    private <T> T handleDeleteRequestException(final RuntimeException e) throws NotFoundException,
            UnauthorizedAccessException, ServerErrorException, ServerUnreachableException {
        if (e instanceof UniformInterfaceException) {
            switch (((UniformInterfaceException) e).getResponse().getClientResponseStatus()) {
            case NOT_FOUND:
                throw new NotFoundException(e);
            case UNAUTHORIZED:
            case FORBIDDEN:
                throw new UnauthorizedAccessException(e);
            default:
                throw new ServerErrorException(e);
            }
        } else if (e instanceof ClientHandlerException) {
            throw new ServerUnreachableException(e);
        }
        throw e;
    }

    public void addCookie(final Cookie cookie) {
        this.cookies.put(cookie.getName(), cookie);
    }

    public static String encode(final String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
