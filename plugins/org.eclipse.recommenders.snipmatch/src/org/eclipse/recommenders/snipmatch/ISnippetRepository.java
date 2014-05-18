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

    List<Recommendation<ISnippet>> search(String query);

    String getRepositoryLocation();

    boolean hasSnippet(UUID uuid);

    boolean delete(UUID uuid) throws IOException;

    boolean isDeleteSupported();

    void importSnippet(ISnippet snippet) throws IOException;

    boolean isImportSupported();
}
