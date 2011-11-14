/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.udc;

public class QueryObject {
    public QueryObject(final Object... keys) {
        if (keys == null) {
            return;
        }
        if (keys.length == 0) {
            return;
        }
        this.keys = keys;
    }

    public Object[] keys;
}
