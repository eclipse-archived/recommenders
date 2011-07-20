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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMember;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.rcp.extdoc.IServerType;
import org.eclipse.recommenders.server.extdoc.ICouchDbServer;

import com.sun.jersey.api.client.GenericType;

class TestCouchDbServer implements ICouchDbServer {

    @Override
    public <T> List<T> getRows(final String view, final Map<String, String> keyParts,
            final GenericType<GenericResultObjectView<T>> resultType) {
        // TODO Auto-generated method stub
        return null;
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

    @Override
    public <T> T getProviderContent(final String providerId, final IMember element,
            final GenericType<GenericResultObjectView<T>> resultType) {
        // TODO Auto-generated method stub
        return null;
    }

}
