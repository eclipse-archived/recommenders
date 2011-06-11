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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.internal.server.extdoc.AbstractRatingsServer;
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.rcp.extdoc.features.Comment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;

public final class WikiServer extends AbstractRatingsServer implements ICommentsServer {

    public String getText(final IJavaElement javaElement) {
        final Map<String, Object> document = Server.getDocument(getDocumentId(javaElement));
        return document == null ? null : (String) document.get("text");
    }

    public void setText(final IJavaElement javaElement, final String text) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("text", text);
        Server.storeOrUpdateDocument(getDocumentId(javaElement), map);
    }

    @Override
    public List<Comment> getComments(final IJavaElement javaElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addComment(final IJavaElement javaElement, final Comment comment) {
        // TODO Auto-generated method stub
    }

    @Override
    protected String getDocumentId(final Object object) {
        return "wiki_"
                + ((IJavaElement) object).getHandleIdentifier().replace("/", "").replace("\\", "").replace("<", "_")
                        .replace("~", "-");
    }

}
