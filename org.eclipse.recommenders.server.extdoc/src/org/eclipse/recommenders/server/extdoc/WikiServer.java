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
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

public final class WikiServer implements IRatingsServer {

    private static final String STARSSUM = "starsSum";
    private static final String STARSCOUNT = "starsCount";

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
        final Map<String, Object> document = Server.getDocument(getId(javaElement));
        return document == null ? 0 : 0;
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
