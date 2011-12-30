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
package org.eclipse.recommenders.mining.extdocs.couch;

import java.util.Set;

import org.eclipse.recommenders.mining.extdocs.ISuperclassProvider;
import org.eclipse.recommenders.mining.extdocs.couch.CouchGuiceModule.Input;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.inject.Inject;

public class CouchSuperclassProvider implements ISuperclassProvider {

    private final CouchDbDataAccess db;
    private Set<ITypeName> _cached;

    @Inject
    public CouchSuperclassProvider(@Input final CouchDbDataAccess db) {
        this.db = db;
    }

    @Override
    public Set<ITypeName> getSuperclasses() {
        if (_cached == null) {
            _cached = db.getSuperclassNames();
        }
        return _cached;
    }

}
