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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.exception.NotFoundException;
import org.svenson.JSONParser;

@SuppressWarnings("unchecked")
final class CouchDB {

    private final Database dbase;

    public CouchDB(final String host, final String database) {
        dbase = new Database(host, database);
    }

    Set<Map<String, Object>> getDocuments() {
        final Set<Map<String, Object>> results = new HashSet<Map<String, Object>>();
        for (final ValueRow<Map> row : dbase.listDocuments(new Options(), new JSONParser()).getRows()) {
            results.add(getDocument(row.getId()));
        }
        return results;
    }

    private Map<String, Object> getDocument(final String docId) {
        return dbase.getDocument(Map.class, docId);
    }

    void updateDocument(final String docId, final Map<String, Object> attributes) {
        Map<String, Object> fields;
        try {
            fields = getDocument(docId);
            fields.putAll(attributes);
        } catch (final NotFoundException e) {
            fields = attributes;
            fields.put("_id", docId);
        }
        storeDocument(docId, fields);
    }

    private void storeDocument(final String docId, final Map<String, Object> attributes) {
        if (attributes.containsKey("_rev")) {
            dbase.updateDocument(attributes);
        } else {
            attributes.put("_id", docId);
            dbase.createDocument(attributes);
        }
    }

}
