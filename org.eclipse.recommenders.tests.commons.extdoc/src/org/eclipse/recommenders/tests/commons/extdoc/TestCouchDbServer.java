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

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IServerType;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;

import com.sun.jersey.api.client.GenericType;

class TestCouchDbServer implements ICouchDbServer {

    @Override
    public <T> List<T> getRows(final String view, final Map<String, String> keyParts,
            final GenericType<GenericResultObjectView<T>> resultType) {
        return new ArrayList<T>();
    }

    @Override
    public void post(final IServerType object) {
        // TODO Auto-generated method stub

    }

    @Override
    public TransactionResult put(final String view, final String documentId, final IServerType object) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProviderContent(final String providerId, final IName element,
            final GenericType<GenericResultObjectView<T>> resultType) {
        final Class<T> clazz = (Class<T>) ((ParameterizedType) resultType.getType()).getActualTypeArguments()[0];
        try {
            return clazz.newInstance();
        } catch (final InstantiationException e) {
            throw new IllegalAccessError();
        } catch (final IllegalAccessException e) {
            throw new IllegalAccessError();
        }
    }

}
