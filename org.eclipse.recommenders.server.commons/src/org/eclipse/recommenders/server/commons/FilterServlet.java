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
package org.eclipse.recommenders.server.commons;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FilterServlet implements Servlet {

    private final Filter filter;
    private final Servlet delegate;
    private ServletConfig config;
    private final FilterChainImpl filterChain;

    public FilterServlet(final Filter filter, final Servlet delegate) {
        this.filter = filter;
        this.delegate = delegate;
        filterChain = new FilterChainImpl();
    }

    @Override
    public void destroy() {
        filter.destroy();
        delegate.destroy();
        config = null;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return delegate.getServletInfo();
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        this.config = config;
        delegate.init(config);
    }

    @Override
    public void service(final ServletRequest request, final ServletResponse response) throws ServletException,
            IOException {
        filter.doFilter(request, response, filterChain);
    }

    private class FilterChainImpl implements FilterChain {

        @Override
        public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException,
                ServletException {
            delegate.service(request, response);
        }
    }

}
