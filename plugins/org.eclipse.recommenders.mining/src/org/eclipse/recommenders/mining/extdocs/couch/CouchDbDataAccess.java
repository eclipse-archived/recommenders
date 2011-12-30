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

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.webclient.CouchUtils.createViewUrl;
import static org.eclipse.recommenders.webclient.CouchUtils.createViewUrlWithKeyObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.webclient.WebServiceClient;
import org.eclipse.recommenders.webclient.results.GenericResultRowView;
import org.eclipse.recommenders.webclient.results.SimpleView;
import org.eclipse.recommenders.webclient.results.TransactionResult;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

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
        final WebResource resource = client.createResource("/_design/compilationunits/_view/bySuperclass");
        final String key = superclass.getIdentifier();
        final GenericType<SimpleView<CompilationUnit>> docType = new GenericType<SimpleView<CompilationUnit>>() {
        };
        return new DocumentsByKey<CompilationUnit>(resource, key, docType);
    }

    public void saveOrUpdate(final ClassOverrideDirectives directives) {
        final Optional<ClassOverrideDirectives> directivesOption = getClassOverrideDirectives(directives.getType());
        if (directivesOption.isPresent()) {
            final ClassOverrideDirectives oldDirectives = directivesOption.get();
            directives._id = oldDirectives._id;
            directives._rev = oldDirectives._rev;
        }
        final TransactionResult result = client.doPostRequest("", directives, TransactionResult.class);
        directives._id = result.id;
        directives._rev = result.rev;
    }

    private Optional<ClassOverrideDirectives> getClassOverrideDirectives(final ITypeName type) {
        final Map<String, String> keyValuePairs = Maps.newLinkedHashMap();
        keyValuePairs.put("providerId", ClassOverrideDirectives.class.getSimpleName());
        keyValuePairs.put("type", type.getIdentifier());
        final String url = createViewUrlWithKeyObject("providers", "providers", keyValuePairs);
        final GenericResultRowView<Key, Map<String, String>, ClassOverrideDirectives> resultView = client.doGetRequest(
                url, new GenericType<GenericResultRowView<Key, Map<String, String>, ClassOverrideDirectives>>() {
                });
        return fromNullable(resultView.getFirstValue(null));
    }

    public void saveOrUpdate(final ClassOverridePatterns pattern) {
        final Optional<ClassOverridePatterns> patternsOption = getClassOverridePatterns(pattern.getType());
        if (patternsOption.isPresent()) {
            final ClassOverridePatterns oldPattern = patternsOption.get();
            pattern._id = oldPattern._id;
            pattern._rev = oldPattern._rev;
        }
        final TransactionResult result = client.doPostRequest("", pattern, TransactionResult.class);
        pattern._id = result.id;
        pattern._rev = result.rev;
    }

    private Optional<ClassOverridePatterns> getClassOverridePatterns(final ITypeName type) {
        final Map<String, String> keyValuePairs = Maps.newLinkedHashMap();
        keyValuePairs.put("providerId", ClassOverridePatterns.class.getSimpleName());
        keyValuePairs.put("type", type.getIdentifier());
        final String url = createViewUrlWithKeyObject("providers", "providers", keyValuePairs);
        final GenericResultRowView<Key, Map<String, String>, ClassOverridePatterns> resultView = client.doGetRequest(
                url, new GenericType<GenericResultRowView<Key, Map<String, String>, ClassOverridePatterns>>() {
                });
        return fromNullable(resultView.getFirstValue(null));
    }

    public void saveOrUpdate(final MethodSelfcallDirectives methodSelfcall) {
        final Optional<MethodSelfcallDirectives> patternsOption = getMethodSelfcallDirectives(methodSelfcall
                .getMethod());
        if (patternsOption.isPresent()) {
            final MethodSelfcallDirectives oldPattern = patternsOption.get();
            methodSelfcall._id = oldPattern._id;
            methodSelfcall._rev = oldPattern._rev;
        }
        final TransactionResult result = client.doPostRequest("", methodSelfcall, TransactionResult.class);
        methodSelfcall._id = result.id;
        methodSelfcall._rev = result.rev;
    }

    private Optional<MethodSelfcallDirectives> getMethodSelfcallDirectives(final IMethodName method) {
        final Map<String, String> keyValuePairs = Maps.newLinkedHashMap();
        keyValuePairs.put("providerId", MethodSelfcallDirectives.class.getSimpleName());
        keyValuePairs.put("method", method.getIdentifier());
        final String url = createViewUrlWithKeyObject("providers", "providers", keyValuePairs);
        final GenericResultRowView<Key, Map<String, String>, MethodSelfcallDirectives> resultView = client
                .doGetRequest(url,
                        new GenericType<GenericResultRowView<Key, Map<String, String>, MethodSelfcallDirectives>>() {
                        });
        return Optional.fromNullable(resultView.getFirstValue(null));
    }

    private static class Key {
        // String providerId;
        // String type;
        // String method;

    }

}
