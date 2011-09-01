/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.extdocs.couch;

import static org.eclipse.recommenders.commons.client.CouchUtils.createViewUrl;
import static org.eclipse.recommenders.commons.client.CouchUtils.createViewUrlWithKey;
import static org.eclipse.recommenders.commons.client.CouchUtils.createViewUrlWithKeyObject;
import static org.eclipse.recommenders.commons.utils.Option.wrap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.commons.client.GenericResultRowView;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public class CouchDbDataAccess {

    private final WebServiceClient client;

    @Inject
    public CouchDbDataAccess(final WebServiceClient client) {
        this.client = client;
    }

    public Set<ITypeName> getSuperclassNames() {
        final Set<ITypeName> result = Sets.newTreeSet();
        final String url = createViewUrl("compilationunits", "bySuperclass") + "?reduce=true&group_level=1";
        final GenericResultRowView<String, Object, Object> resultView = client.doGetRequest(url,
                new GenericType<GenericResultRowView<String, Object, Object>>() {
                });
        final List<String> keys = resultView.getTransformedKeys();
        for (final String key : keys) {
            if (key != null) {
                result.add(VmTypeName.get(key));
            }
        }
        return result;
    }

    public Iterable<CompilationUnit> getCompilationUnitsForSuperclass(final ITypeName superclass) {
        final String url = createViewUrlWithKey("compilationunits", "bySuperclass", superclass.getIdentifier())
                + "&reduce=false&include_docs=true";
        final GenericResultRowView<Object, CompilationUnit, Object> resultView = client.doGetRequest(url,
                new GenericType<GenericResultRowView<Object, CompilationUnit, Object>>() {
                });
        return resultView.getTransformedDocs();
    }

    public void saveOrUpdate(final ClassOverrideDirectives directives) {
        final Option<ClassOverrideDirectives> directivesOption = getClassOverrideDirectives(directives.getType());
        if (directivesOption.hasValue()) {
            final ClassOverrideDirectives oldDirectives = directivesOption.get();
            directives._id = oldDirectives._id;
            directives._rev = oldDirectives._rev;
        }
        final TransactionResult result = client.doPostRequest("", directives, TransactionResult.class);
        directives._id = result.id;
        directives._rev = result.rev;
    }

    private Option<ClassOverrideDirectives> getClassOverrideDirectives(final ITypeName type) {
        final Map<String, String> keyValuePairs = Maps.newLinkedHashMap();
        keyValuePairs.put("providerId", ClassOverrideDirectives.class.getSimpleName());
        keyValuePairs.put("type", type.getIdentifier());
        final String url = createViewUrlWithKeyObject("providers", "providers", keyValuePairs);
        final GenericResultRowView<Key, Map<String, String>, ClassOverrideDirectives> resultView = client.doGetRequest(
                url, new GenericType<GenericResultRowView<Key, Map<String, String>, ClassOverrideDirectives>>() {
                });
        return wrap(resultView.getFirstValue(null));
    }

    private static class Key {
        // String providerId;
        // String type;
        // String method;

    }

}
