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

import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.google.common.collect.Sets;

public class PrefixQueryParser extends QueryParser {

    private final Set<String> prefixFields;

    public PrefixQueryParser(Version matchVersion, String f, Analyzer a, String... prefixFields) {
        super(matchVersion, f, a);
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
