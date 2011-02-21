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
package org.eclipse.recommenders.server.codesearch.lucene;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.search.Explanation;

public interface SingleFeatureScorer {

    Set<Integer> findRelevantDocs() throws IOException;

    float scoreDoc(int doc) throws IOException;

    Explanation explainScore(int doc) throws IOException;

    String getIdentifier();
}
