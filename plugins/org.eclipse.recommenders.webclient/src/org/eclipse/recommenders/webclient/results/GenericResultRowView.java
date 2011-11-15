/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.webclient.results;

import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class GenericResultRowView<Key, Doc, Value> {

    public int total_rows;
    public int offset;
    public List<ResultRow<Key, Doc, Value>> rows;

    public List<Value> getTransformedValues() {
        final List<Value> resultList = Lists.newLinkedList();
        for (final ResultRow<Key, Doc, Value> row : rows) {
            resultList.add(row.value);
        }
        return resultList;
    }

    public List<Key> getTransformedKeys() {
        final List<Key> resultList = Lists.newLinkedList();
        for (final ResultRow<Key, Doc, Value> row : rows) {
            resultList.add(row.key);
        }
        return resultList;
    }

    public List<Doc> getTransformedDocs() {
        final List<Doc> resultList = Lists.newLinkedList();
        for (final ResultRow<Key, Doc, Value> row : rows) {
            resultList.add(row.doc);
        }
        return resultList;
    }

    public Value getFirstValue(final Value defaultValue) {
        final List<Value> res = getTransformedValues();
        return Iterables.getFirst(res, defaultValue);
    }

    public Key getFirstKey(final Key defaultKey) {
        final List<Key> res = getTransformedKeys();
        return Iterables.getFirst(res, defaultKey);
    }

    public Doc getFirstDoc(final Doc defaultDoc) {
        final List<Doc> res = getTransformedDocs();
        return Iterables.getFirst(res, defaultDoc);
    }
    

	public String getHighestDocId() {
		try {
			return Iterables.getLast(rows).id;
		} catch(NoSuchElementException e) {
			return "";
		}
	}
}