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
package org.eclipse.recommenders.server.extdoc;

import java.util.List;

import com.sun.jersey.api.client.GenericType;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.internal.server.extdoc.AbstractRatingsServer;
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.server.extdoc.types.WikiEntry;

public final class WikiServer extends AbstractRatingsServer implements ICommentsServer {

    private static final String PROVIDERID = WikiEntry.class.getSimpleName();

    public String getText(final IJavaElement javaElement) {
        final WikiEntry entry = getEntry(javaElement);
        return entry == null ? null : entry.getText();
    }

    public void setText(final IJavaElement javaElement, final String text) {
        WikiEntry entry = getEntry(javaElement);
        if (entry == null) {
            entry = WikiEntry.create(javaElement, text);
        } else {
            entry.setText(text);
        }
        Server.post(entry);
    }

    @Override
    public List<IComment> getComments(final Object object) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IComment addComment(final Object object, final String text) {
        // TODO Auto-generated method stub
        return null;
    }

    private WikiEntry getEntry(final IJavaElement javaElement) {
        final String key = javaElement.getHandleIdentifier();
        final WikiEntry result = Server.getProviderContent(PROVIDERID, "method", key,
                new GenericType<GenericResultObjectView<WikiEntry>>() {
                });
        return result;
    }

}
