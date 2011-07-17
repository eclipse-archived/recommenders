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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;

import com.sun.jersey.api.client.GenericType;

public interface ICouchDbServer {

    <T> List<T> getRows(String view, Map<String, String> key, GenericType<GenericResultObjectView<T>> resultType);

    void post(Object object);

    void delete(String view, Map<String, String> key, String rev);

    <T> T getProviderContent(String providerId, String key, String value,
            GenericType<GenericResultObjectView<T>> resultType);

    String createKey(IType type);

    String createKey(IMethod method);

}
