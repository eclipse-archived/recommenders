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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.eclipse.core.runtime.FileLocator;

final class CouchDBCache {

    private final Map<String, Map<String, Object>> cache = new HashMap<String, Map<String, Object>>();
    private final Map<String, Map<String, Object>> changes = new HashMap<String, Map<String, Object>>();

    private String filename;
    private ZipFile file;
    private final Gson gson = new GsonBuilder().create();

    CouchDBCache() {
        try {
            filename = FileLocator.resolve(ExtDocServerPlugin.getBundleEntry("data/extdoc.zip")).getFile();
            file = new ZipFile(filename);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    Map<String, Object> getDocument(final String docId) {
        if (!cache.containsKey(docId)) {
            cache.put(docId, loadFromDisk(docId));
        }
        return cache.get(docId);
    }

    void storeOrUpdateDocument(final String docId, final Map<String, Object> attributes) {
        if (cache.get(docId) == null) {
            cache.put(docId, attributes);
        } else {
            cache.get(docId).putAll(attributes);
        }
        if (changes.containsKey(docId)) {
            changes.get(docId).putAll(attributes);
        } else {
            changes.put(docId, attributes);
        }
    }

    private Map<String, Object> loadFromDisk(final String docId) {
        Map<String, Object> result = null;
        final ZipEntry entry = file.getEntry(docId + ".json");
        if (entry != null) {
            try {
                final Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(entry)));
                result = gson.fromJson(reader, new TypeToken<Map<String, String>>() {
                }.getType());
            } catch (final IOException e) {
            }
        }
        return result;
    }

    void synchronize(final CouchDB database) {
        storeToCouchDB(database);
        cache.clear();
        loadFromCouchDB(database);
        changes.clear();
    }

    void clear() {
        cache.clear();
        changes.clear();
    }

    private void storeToCouchDB(final CouchDB database) {
        for (final Entry<String, Map<String, Object>> entry : changes.entrySet()) {
            database.updateDocument(entry.getKey(), entry.getValue());
        }
    }

    private void loadFromCouchDB(final CouchDB database) {
        try {
            final FileOutputStream fos = new FileOutputStream(filename);
            final ZipOutputStream zos = new ZipOutputStream(fos);
            for (final Map<String, Object> entry : database.getDocuments()) {
                final String entryId = (String) entry.get("_id");
                final String json = gson.toJson(entry);
                zos.putNextEntry(new ZipEntry(entryId + ".json"));
                zos.write(json.getBytes());
                cache.put(entryId, entry);
            }
            zos.close();
            fos.close();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
