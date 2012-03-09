/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.mining.extdocs.couch;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.recommenders.webclient.results.SimpleView;
import org.slf4j.Logger;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DocumentsByKey<T> implements Iterable<T> {

    private final Logger log = getLogger(getClass());

    private final WebResource resource;
    private final GenericType<SimpleView<T>> docType;
    private int numberOfDocumentsPerRequest = 10;

    private final String startKey;
    private final String endKey;

    public DocumentsByKey(WebResource resource, String key, GenericType<SimpleView<T>> docType) {
        this(resource, key, key, docType);
    }

    public DocumentsByKey(WebResource resource, String startKey, String endKey, GenericType<SimpleView<T>> docType) {
        this.resource = resource;
        this.docType = docType;
        this.startKey = startKey;
        this.endKey = endKey;
    }

    public void setNumberOfDocumentsPerRequest(int num) {
        numberOfDocumentsPerRequest = num;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private String currentDocId;

            private boolean isDone = false;
            private List<T> buffer = new ArrayList<T>();

            @Override
            public boolean hasNext() {
                if (!buffer.isEmpty()) {
                    return true;
                } else {
                    if (isDone) {
                        return false;
                    } else {
                        requestNext();
                        return !buffer.isEmpty();
                    }
                }
            }

            private void requestNext() {
                MultivaluedMapImpl params = new MultivaluedMapImpl();
                params.add("limit", numberOfDocumentsPerRequest);
                params.add("include_docs", "true");
                params.add("reduce", "false");
                params.add("startkey", quote(startKey));
                params.add("endkey", quote(endKey));
                if (currentDocId != null) {
                    params.add("startkey_docid", currentDocId);
                    params.add("skip", 1);
                }
                log.debug("requesting next: {}", resource.queryParams(params).getURI());
                Builder b = resource.queryParams(params).type(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE);

                SimpleView<T> docs = b.get(docType);
                currentDocId = docs.getHighestDocId();
                buffer.addAll(docs.getTransformedDocs());
            }

            private String quote(String s) {
                try {
                    return "\"" + URLEncoder.encode(s, "UTF-8") + "\"";
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T next() {
                return buffer.remove(0);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}