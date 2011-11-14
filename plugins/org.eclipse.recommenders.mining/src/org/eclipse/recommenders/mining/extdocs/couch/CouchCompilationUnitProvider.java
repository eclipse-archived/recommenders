/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.extdocs.couch;

import static org.eclipse.recommenders.utils.names.VmTypeName.OBJECT;

import java.util.Collections;

import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.mining.extdocs.ICompilationUnitProvider;
import org.eclipse.recommenders.mining.extdocs.couch.CouchGuiceModule.Input;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.inject.Inject;

public class CouchCompilationUnitProvider implements ICompilationUnitProvider {

    private final CouchDbDataAccess db;

    @Inject
    public CouchCompilationUnitProvider(@Input final CouchDbDataAccess db) {
        this.db = db;
    }

    @Override
    public Iterable<CompilationUnit> getCompilationUnits(final ITypeName superclass) {
        if (OBJECT.equals(superclass)) {
            return Collections.emptyList();
        }
        return db.getCompilationUnitsForSuperclass(superclass);
    }
}
