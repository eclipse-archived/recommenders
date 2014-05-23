/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.Recommendation;

import com.google.common.collect.ImmutableSet;

public interface ISnippetRepository extends Openable, Closeable {

    ImmutableSet<Recommendation<ISnippet>> getSnippets();

    /**
     * Returns <b>all</b> snippets matching the search query. Does not use timeouts or relevance thresholds.
     *
     * This method may block for some time.
     *
     * @return a list of all snippets matching the search query
     */
    List<Recommendation<ISnippet>> search(String query);

    /**
     * Returns <b>at most</b> <code>maxResults</code> snippets matching the search query. Implementations may return
     * less than the specified number of snippets at their own discretion. This may be due to performance reasons,
     * timeouts or relevance thresholds.
     *
     * Implementations should provide results quickly, even if this means returning less than <code>maxResults</code>
     * snippets.
     *
     * @return a list of snippets matching the search query
     */
    List<Recommendation<ISnippet>> search(String query, int maxResults);

    String getRepositoryLocation();

    boolean hasSnippet(UUID uuid);

    boolean delete(UUID uuid) throws IOException;

    boolean isDeleteSupported();

    void importSnippet(ISnippet snippet) throws IOException;

    boolean isImportSupported();
}
