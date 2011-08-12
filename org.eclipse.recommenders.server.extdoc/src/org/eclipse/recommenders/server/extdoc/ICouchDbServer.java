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
package org.eclipse.recommenders.server.extdoc;

import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IServerType;

import com.sun.jersey.api.client.GenericType;

public interface ICouchDbServer {

    <T> List<T> getRows(String view, Map<String, String> keyParts, GenericType<GenericResultObjectView<T>> resultType);

    void post(IServerType object);

    TransactionResult put(String documentId, IServerType object);

    <T> T getProviderContent(String providerId, IName element, GenericType<GenericResultObjectView<T>> resultType);

}
