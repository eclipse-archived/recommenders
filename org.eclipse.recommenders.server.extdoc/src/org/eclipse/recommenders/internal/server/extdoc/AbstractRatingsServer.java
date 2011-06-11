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
package org.eclipse.recommenders.internal.server.extdoc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;

public abstract class AbstractRatingsServer implements IStarsRatingsServer {

    private static final String STARSSUM = "starsSum";
    private static final String STARSCOUNT = "starsCount";

    private final Map<Object, Integer> userRatings = new HashMap<Object, Integer>();

    @Override
    public final int getAverageRating(final Object object) {
        final Map<String, Object> document = Server.getDocument(getDocumentId(object));
        final int starsCount = document == null ? 0 : getStarsCount(document);
        return starsCount == 0 ? 0 : getStarsSum(document) / starsCount;
    }

    @Override
    public final int getUserRating(final Object object) {
        return userRatings.containsKey(object) ? userRatings.get(object) : -1;
    }

    @Override
    public final void addRating(final Object object, final int stars) {
        final String docId = getDocumentId(object);
        Map<String, Object> document = Server.getDocument(docId);

        if (document == null) {
            document = new HashMap<String, Object>();
        }
        document.put(STARSCOUNT, getStarsCount(document) + 1);
        document.put(STARSSUM, getStarsSum(document) + stars);

        Server.storeOrUpdateDocument(docId, document);
        userRatings.put(object, stars);
    }

    protected abstract String getDocumentId(Object object);

    private int getStarsCount(final Map<String, Object> document) {
        final Object object = document.get(STARSCOUNT);
        return object == null ? 0 : Integer.parseInt(String.valueOf(object));
    }

    private int getStarsSum(final Map<String, Object> document) {
        final Object object = document.get(STARSSUM);
        return object == null ? 0 : Integer.parseInt(String.valueOf(object));
    }

}
