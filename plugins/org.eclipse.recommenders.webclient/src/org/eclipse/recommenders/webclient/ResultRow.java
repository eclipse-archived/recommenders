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
package org.eclipse.recommenders.webclient;

public class ResultRow<Key, Doc, Value> {

    public static <Key, Doc, Value> ResultRow<Key, Doc, Value> create(final String id, final Value value) {
        final ResultRow<Key, Doc, Value> res = new ResultRow<Key, Doc, Value>();
        res.id = id;
        res.value = value;
        return res;
    }

    public String id;
    public Key key;
    public Doc doc;
    public Value value;
}
