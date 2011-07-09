/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc.features;

import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Preconditions;

final class CommentsComposite {

    private final IProvider provider;
    private final ICommentsServer server;
    private final Object object;

    private Composite composite;

    CommentsComposite(final Composite parent, final Object object, final IProvider provider,
            final ICommentsServer server) {
        this.provider = provider;
        this.server = Preconditions.checkNotNull(server);
        this.object = object;

        createGrid(parent);
    }

    private void createGrid(final Composite parent) {
        composite = new Composite(parent, SWT.NONE);
    }

    private void loadComments() {
        for (final IComment comment : server.getComments(object, provider)) {
            // TODO: ...
        }
    }

    private void displayAddComment() {

    }

    private void addComment() {
        // TODO: ...
        final String text = null;
        final IComment comment = server.addComment(object, text, provider);
        // TODO: ...
    }
}
