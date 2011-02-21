/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class TypesUsesMethodCallsGrouper {
    private final LinkedHashMultimap<ITypeName, IMethodName> index = LinkedHashMultimap.create();
    private final Request request;

    public TypesUsesMethodCallsGrouper(final Request request, final Proposal hit) {
        this.request = request;
        buildIndex(hit);
    }

    @SuppressWarnings("unchecked")
    private void buildIndex(final Proposal hit) {
        final Collection<ITypeName> inBoth = CollectionUtils.retainAll(hit.usedTypes, request.usedTypes);
        for (final ITypeName type : inBoth) {
            index.putAll(type, Collections.EMPTY_LIST);
        }
        for (final ITypeName type : hit.usedTypes) {
            index.putAll(type, Collections.EMPTY_LIST);
        }
        for (final IMethodName method : hit.calledMethods) {
            final ITypeName type = method.getDeclaringType();
            index.put(type, method);
        }
    }

    public boolean inRequest(final ITypeName type) {
        return request.usedTypes.contains(type);
    }

    public boolean inRequest(final IMethodName method) {
        return request.calledMethods.contains(method);
    }

    public List<ITypeName> getTypes(final boolean foundInRequest) {
        final List<ITypeName> list = new ArrayList<ITypeName>();
        for (final ITypeName type : index.keySet()) {
            if (inRequest(type) && foundInRequest) {
                int count = 0;
                final Collection<IMethodName> methodGroup = index.get(type);
                for (final IMethodName method : methodGroup) {
                    if (inRequest(method)) {
                        count++;
                    }
                }
                if (count > 0) {
                    list.add(0, type);
                } else {
                    list.add(list.size(), type);
                }
            } else if (!foundInRequest) {
                if (inRequest(type)) {
                    continue;
                }
                list.add(type);
            }
        }
        return list;
    }

    public Multimap<ITypeName, IMethodName> getGroups() {
        return index;
    }
}
