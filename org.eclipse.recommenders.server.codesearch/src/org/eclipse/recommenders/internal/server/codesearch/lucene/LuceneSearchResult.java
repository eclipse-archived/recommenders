/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.codesearch.lucene;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

public class LuceneSearchResult {

    public float score;
    public String snippetId;
    public int luceneDocumentId;
    public ScoringExplanation explanation;

    public static LuceneSearchResult create(final float score, final String snippetId, final int luceneDocumentId,
            final ScoringExplanation explanation) {
        ensureIsNotNull(snippetId, "null snippet id is invalid");
        final LuceneSearchResult res = new LuceneSearchResult();
        res.score = score;
        res.snippetId = snippetId;
        res.luceneDocumentId = luceneDocumentId;
        res.explanation = explanation;
        return res;
    }

}
