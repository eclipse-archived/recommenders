/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.commons.extdoc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;

import com.sun.jersey.api.client.GenericType;

class TestCouchDbServer implements ICouchDbServer {

    @Override
    public <T> List<T> getRows(final String view, final Map<String, String> key,
            final GenericType<GenericResultObjectView<T>> resultType) {
        return Collections.emptyList();
    }

    @Override
    public void post(final Object object) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> T getProviderContent(final String providerId, final String key, final String value,
            final GenericType<GenericResultObjectView<T>> resultType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createKey(final IType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createKey(final IMethod method) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void put(final String view, final Map<String, String> key, final String rev, final Object object) {
        // TODO Auto-generated method stub

    }

}
