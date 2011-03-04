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
package org.eclipse.recommenders.server.codesearch;

public class SearchResult {

    public float score;
    public String snippetId;

    public SearchResult(final float score, final String snippetId) {
        this.score = score;
        this.snippetId = snippetId;
    }

}
