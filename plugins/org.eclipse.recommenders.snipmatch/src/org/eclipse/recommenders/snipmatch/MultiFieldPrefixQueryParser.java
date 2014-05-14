/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.google.common.collect.Sets;

public class MultiFieldPrefixQueryParser extends MultiFieldQueryParser {

    private final Set<String> prefixFields;

    public MultiFieldPrefixQueryParser(Version matchVersion, String[] fields, Analyzer a, Map<String, Float> boosts,
            String... prefixFields) {
        super(matchVersion, fields, a, boosts);
        this.prefixFields = Sets.newHashSet(prefixFields);
    }

    @Override
    protected Query newTermQuery(Term term) {
        if (prefixFields.contains(term.field())) {
            return newPrefixQuery(term);
        }
        return super.newTermQuery(term);
    }
}
