/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch.rcp;

import org.eclipse.recommenders.snipmatch.ISnippetRepository;

/**
 * Triggered when the snippet repository was opened to inform clients that the snippet repository is available.
 * <p>
 * Clients of this event may consider refreshing themselves whenever they receive this event. Clients get notified in a
 * background process.
 */
public class SnippetRepositoryOpenedEvent {

    private final ISnippetRepository repo;

    public SnippetRepositoryOpenedEvent(ISnippetRepository repo) {
        this.repo = repo;
    }

    public ISnippetRepository getRepository() {
        return repo;
    }
}
