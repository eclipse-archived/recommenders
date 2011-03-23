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
package org.eclipse.recommenders.internal.server.codesearch.lucene;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.eclipse.recommenders.commons.codesearch.FeatureWeights;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.Lists;

public class LuceneQueryUtil {

    public static final String FIELD_CALLS = "calls";
    public static final String FIELD_CLASS = "class";
    public static final String FIELD_METHOD = "method";
    public static final String FIELD_USES = "uses";
    public static final String FIELD_FIELDS = "fields";
    public static final String FIELD_OVERRIDES = "overrides";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_IMPLEMENTS = "implements";
    public static final String FIELD_EXTENDS = "extends";
    public static final String FIELD_SUPERCLASS = "superclass";

    public static List<Term> createTerms(final String fieldName, final Collection<? extends IName> values) {
        final List<Term> terms = Lists.newArrayList();
        for (final IName value : values) {
            terms.add(createTerm(fieldName, value));
        }
        return terms;
    }

    public static Term createTerm(final String fieldName, final IName value) {
        return new Term(fieldName, value.getIdentifier());
    }

    public static Term createUsesTerm(final ITypeName type) {
        return createTerm(FIELD_USES, type);
    }

    public static List<Term> createUsesTerms(final Collection<ITypeName> types) {
        return createTerms(FIELD_USES, types);
    }

    public static Term createFieldTerm(final ITypeName type) {
        return createTerm(FIELD_FIELDS, type);
    }

    public static List<Term> createFieldTerms(final Collection<ITypeName> types) {
        return createTerms(FIELD_FIELDS, types);
    }

    public static Term createCallsTerm(final IMethodName method) {
        return createTerm(FIELD_CALLS, method);
    }

    public static List<Term> createCallsTerm(final Collection<IMethodName> methods) {
        return createTerms(FIELD_CALLS, methods);
    }

    public static Term createOverridesTerm(final IMethodName method) {
        return createTerm(FIELD_OVERRIDES, method);
    }

    public static List<Term> createOverrideTerms(final Collection<IMethodName> methods) {
        return createTerms(FIELD_OVERRIDES, methods);
    }

    public static Term createExtendsTerm(final ITypeName type) {
        return createTerm(FIELD_EXTENDS, type);
    }

    public static List<Term> createExtendsTerms(final Collection<ITypeName> types) {
        return createTerms(FIELD_EXTENDS, types);
    }

    public static Term createImplementsTerm(final ITypeName ITypeName) {
        return new Term(FIELD_IMPLEMENTS, ITypeName.getIdentifier());
    }

    public static List<Term> createImplementsTerms(final Collection<ITypeName> types) {
        return createTerms(FIELD_IMPLEMENTS, types);
    }

    public static Query toQuery(final Request request) {
        final BooleanQuery query = new BooleanQuery();
        for (final ITypeName superclassRef : request.query.extendedTypes) {
            final String superclass = superclassRef.getIdentifier();
            query.add(createTermQuery(FIELD_EXTENDS, superclass), Occur.SHOULD);
        }
        for (final ITypeName interfaceRef : request.query.implementedTypes) {
            final String interfaceName = interfaceRef.getIdentifier();
            query.add(createTermQuery(FIELD_IMPLEMENTS, interfaceName), Occur.SHOULD);
        }
        for (final ITypeName fieldTypeRef : request.query.fieldTypes) {
            final String ITypeName = fieldTypeRef.getIdentifier();
            query.add(createTermQuery(FIELD_FIELDS, ITypeName), Occur.SHOULD);
        }
        for (final IMethodName overriddenMethodRef : request.query.overriddenMethods) {
            final String IMethodName = overriddenMethodRef.getIdentifier();
            query.add(createTermQuery(FIELD_OVERRIDES, IMethodName), Occur.SHOULD);
        }
        // //////////
        for (final ITypeName usedTypeRef : request.query.usedTypes) {
            final String ITypeName = usedTypeRef.getIdentifier();
            final TermQuery q = createTermQuery(FIELD_USES, ITypeName);
            q.setBoost(1.8f);
            query.add(q, Occur.SHOULD);
        }
        for (final IMethodName calledMethodRef : request.query.calledMethods) {
            final String IMethodName = calledMethodRef.getIdentifier();
            final TermQuery q = createTermQuery(FIELD_CALLS, IMethodName);
            q.setBoost(1.8f);
            query.add(q, Occur.SHOULD);
        }
        return query;
    }

    public static CodesearchQuery toCodeSearchQuery(final Request request, final FeatureWeights weights) {
        return new CodesearchQuery(request, weights);
    }

    private static TermQuery createTermQuery(final String field, final String value) {
        return new TermQuery(new Term(field, value));
    }
}
