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
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.rcp.extdoc.features.Comment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;
import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;

public final class WikiServer implements IStarsRatingsServer, ICommentsServer {

    private static final String STARSSUM = "starsSum";
    private static final String STARSCOUNT = "starsCount";

    private final Map<IJavaElement, Integer> userRatings = new HashMap<IJavaElement, Integer>();

    public String getText(final IJavaElement javaElement) {
        final Map<String, Object> document = Server.getDocument(getId(javaElement));
        return document == null ? null : (String) document.get("text");
    }

    public void setText(final IJavaElement javaElement, final String text) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("text", text);
        Server.storeOrUpdateDocument(getId(javaElement), map);
    }

    @Override
    public int getAverageRating(final IJavaElement javaElement) {
        final Map<String, Object> document = Server.getDocument(getId(javaElement));
        final int starsCount = document == null ? 0 : getStarsCount(document);
        return starsCount == 0 ? 0 : getStarsSum(document) / starsCount;
    }

    @Override
    public int getUserRating(final IJavaElement javaElement) {
        return userRatings.containsKey(javaElement) ? userRatings.get(javaElement) : -1;
    }

    @Override
    public void addRating(final IJavaElement javaElement, final int stars) {
        final String documentId = getId(javaElement);
        Map<String, Object> document = Server.getDocument(documentId);

        if (document == null) {
            document = new HashMap<String, Object>();
        }
        document.put(STARSCOUNT, getStarsCount(document) + 1);
        document.put(STARSSUM, getStarsSum(document) + stars);

        Server.storeOrUpdateDocument(documentId, document);
        userRatings.put(javaElement, stars);
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

    private String getId(final IJavaElement javaElement) {
        return "wiki_"
                + javaElement.getHandleIdentifier().replace("/", "").replace("\\", "").replace("<", "_")
                        .replace("~", "-");
    }

    private int getStarsCount(final Map<String, Object> document) {
        final Object object = document.get(STARSCOUNT);
        return object == null ? 0 : Integer.parseInt(String.valueOf(object));
    }

    private int getStarsSum(final Map<String, Object> document) {
        final Object object = document.get(STARSSUM);
        return object == null ? 0 : Integer.parseInt(String.valueOf(object));
    }

}
